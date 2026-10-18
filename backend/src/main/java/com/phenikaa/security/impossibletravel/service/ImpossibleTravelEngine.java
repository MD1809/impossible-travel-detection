package com.phenikaa.security.impossibletravel.service;

import com.phenikaa.security.impossibletravel.dto.response.DetectionResultDto;
import com.phenikaa.security.impossibletravel.dto.response.GeoLocationDto;
import com.phenikaa.security.impossibletravel.entity.*;
import com.phenikaa.security.impossibletravel.repository.LoginHistoryRepository;
import com.phenikaa.security.impossibletravel.repository.SystemConfigRepository;
import com.phenikaa.security.impossibletravel.repository.UserSessionRepository;
import com.phenikaa.security.impossibletravel.util.HaversineCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Động cơ phát hiện di chuyển bất khả thi (Impossible Travel Detection Engine)
 * Đánh giá rủi ro theo 2 kịch bản trọng tâm:
 *  - Kịch bản 1: Concurrent Login Anomaly (Xung đột với phiên đang hoạt động)
 *  - Kịch bản 2: Sequential Impossible Travel (Di chuyển bất khả thi nối tiếp)
 */
@Service
public class ImpossibleTravelEngine {

    private static final Logger log = LoggerFactory.getLogger(ImpossibleTravelEngine.class);

    public static final double DEFAULT_SPEED_THRESHOLD_KMH = 900.0; // Tốc độ tối đa máy bay thương mại
    public static final double MIN_DISTANCE_THRESHOLD_KM = 50.0;    // Bỏ qua di chuyển nội đô dưới 50km
    public static final long INSTANTANEOUS_CONCURRENT_SECONDS = 10; // Ngưỡng đồng thời tức thì (<10s)

    private final UserSessionRepository userSessionRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final SystemConfigRepository systemConfigRepository;

    public ImpossibleTravelEngine(
            UserSessionRepository userSessionRepository,
            LoginHistoryRepository loginHistoryRepository,
            SystemConfigRepository systemConfigRepository) {
        this.userSessionRepository = userSessionRepository;
        this.loginHistoryRepository = loginHistoryRepository;
        this.systemConfigRepository = systemConfigRepository;
    }

    /**
     * Lấy ngưỡng tốc độ tối đa từ cấu hình hệ thống (bảng system_configs)
     */
    public double getSpeedThreshold() {
        try {
            Optional<SystemConfig> configOpt = systemConfigRepository.findByConfigKey("MAX_SPEED_THRESHOLD_KMH");
            if (configOpt.isPresent()) {
                return Double.parseDouble(configOpt.get().getConfigValue());
            }
        } catch (Exception e) {
            log.warn("Không thể tải cấu hình MAX_SPEED_THRESHOLD_KMH từ DB, sử dụng mặc định: {}", DEFAULT_SPEED_THRESHOLD_KMH);
        }
        return DEFAULT_SPEED_THRESHOLD_KMH;
    }

    /**
     * Đánh giá rủi ro cho lần đăng nhập hiện tại
     *
     * @param userId      ID người dùng
     * @param currentLoc  Vị trí địa lý giải mã từ IP hiện tại
     * @param currentTime Thời điểm đăng nhập hiện tại
     * @return Kết quả đánh giá DetectionResultDto (Allowed hoặc Blocked kèm chi tiết)
     */
    public DetectionResultDto evaluate(Long userId, GeoLocationDto currentLoc, LocalDateTime currentTime) {
        double threshold = getSpeedThreshold();

        if (currentLoc == null || currentLoc.getLatitude() == null || currentLoc.getLongitude() == null) {
            log.warn("Không xác định được tọa độ IP hiện tại cho userId: {}, bỏ qua kiểm tra", userId);
            return DetectionResultDto.allowed(threshold);
        }

        // =========================================================================
        // KỊCH BẢN 1: CONCURRENT LOGIN ANOMALY (Xung đột với các phiên đang Active)
        // =========================================================================
        List<UserSession> activeSessions = userSessionRepository.findByUserIdAndIsActiveTrue(userId);
        if (activeSessions != null && !activeSessions.isEmpty()) {
            for (UserSession session : activeSessions) {
                if (session.getLatitude() == null || session.getLongitude() == null) {
                    continue;
                }

                double distanceKm = HaversineCalculator.calculateDistance(
                        currentLoc.getLatitude(), currentLoc.getLongitude(),
                        session.getLatitude(), session.getLongitude()
                );

                LocalDateTime sessionActivity = session.getLastActivity() != null ? session.getLastActivity() : session.getLoginTime();
                long timeDiffSeconds = sessionActivity != null
                        ? Math.abs(Duration.between(sessionActivity, currentTime).getSeconds())
                        : 0;

                if (distanceKm > MIN_DISTANCE_THRESHOLD_KM) {
                    double speedKmh = HaversineCalculator.calculateSpeed(distanceKm, timeDiffSeconds);

                    if (timeDiffSeconds < INSTANTANEOUS_CONCURRENT_SECONDS || speedKmh > threshold) {
                        Severity severity = (timeDiffSeconds < INSTANTANEOUS_CONCURRENT_SECONDS || speedKmh > 2000.0)
                                ? Severity.CRITICAL
                                : Severity.HIGH;

                        String reason = String.format(
                                "Phát hiện phiên đăng nhập đồng thời bất thường giữa %s và %s: Khoảng cách %.2f km trong %d giây (Tốc độ: %.2f km/h > ngưỡng %.2f km/h)",
                                currentLoc.getCity() != null ? currentLoc.getCity() : "Vị trí mới",
                                session.getCity() != null ? session.getCity() : "Phiên hiện tại",
                                distanceKm, timeDiffSeconds, speedKmh, threshold
                        );

                        log.warn("ALERT: {} - User ID: {}", reason, userId);

                        DetectionResultDto result = DetectionResultDto.blocked(
                                EventType.CONCURRENT_IMPOSSIBLE_TRAVEL,
                                severity,
                                distanceKm,
                                timeDiffSeconds,
                                speedKmh,
                                threshold,
                                reason
                        );
                        populateLocations(result, currentLoc, currentTime, session.getIpAddress(), session.getCity(),
                                session.getCountry(), session.getLatitude(), session.getLongitude(), sessionActivity);
                        return result;
                    }
                }
            }
        }

        // =========================================================================
        // KỊCH BẢN 2: SEQUENTIAL IMPOSSIBLE TRAVEL (Di chuyển bất khả thi nối tiếp)
        // =========================================================================
        Optional<LoginHistory> lastLoginOpt = loginHistoryRepository
                .findFirstByUserIdAndStatusOrderByLoginTimeDesc(userId, LoginStatus.SUCCESS);

        if (lastLoginOpt.isPresent()) {
            LoginHistory lastLogin = lastLoginOpt.get();
            if (lastLogin.getLatitude() != null && lastLogin.getLongitude() != null) {
                double distanceKm = HaversineCalculator.calculateDistance(
                        currentLoc.getLatitude(), currentLoc.getLongitude(),
                        lastLogin.getLatitude(), lastLogin.getLongitude()
                );

                LocalDateTime lastLoginTime = lastLogin.getLoginTime();
                long timeDiffSeconds = lastLoginTime != null
                        ? Duration.between(lastLoginTime, currentTime).getSeconds()
                        : 0;

                if (distanceKm > MIN_DISTANCE_THRESHOLD_KM) {
                    if (timeDiffSeconds <= 0) {
                        // Thời gian lần đăng nhập mới lại trước hoặc bằng lần trước nhưng khoảng cách xa -> Bất thường
                        String reason = String.format(
                                "Thời gian đăng nhập bất thường (Δt <= 0s) qua khoảng cách %.2f km", distanceKm);
                        DetectionResultDto result = DetectionResultDto.blocked(
                                EventType.SEQUENTIAL_IMPOSSIBLE_TRAVEL,
                                Severity.HIGH,
                                distanceKm,
                                0,
                                Double.POSITIVE_INFINITY,
                                threshold,
                                reason
                        );
                        populateLocations(result, currentLoc, currentTime, lastLogin.getIpAddress(), lastLogin.getCity(),
                                lastLogin.getCountry(), lastLogin.getLatitude(), lastLogin.getLongitude(), lastLoginTime);
                        return result;
                    }

                    double speedKmh = HaversineCalculator.calculateSpeed(distanceKm, timeDiffSeconds);
                    if (speedKmh > threshold) {
                        Severity severity = (speedKmh > 2000.0 || timeDiffSeconds < 60)
                                ? Severity.CRITICAL
                                : Severity.HIGH;

                        String reason = String.format(
                                "Phát hiện di chuyển bất khả thi nối tiếp từ %s đến %s: Khoảng cách %.2f km sau %d giây (Tốc độ tính toán: %.2f km/h > ngưỡng %.2f km/h)",
                                lastLogin.getCity() != null ? lastLogin.getCity() : "Vị trí trước",
                                currentLoc.getCity() != null ? currentLoc.getCity() : "Vị trí mới",
                                distanceKm, timeDiffSeconds, speedKmh, threshold
                        );

                        log.warn("ALERT: {} - User ID: {}", reason, userId);

                        DetectionResultDto result = DetectionResultDto.blocked(
                                EventType.SEQUENTIAL_IMPOSSIBLE_TRAVEL,
                                severity,
                                distanceKm,
                                timeDiffSeconds,
                                speedKmh,
                                threshold,
                                reason
                        );
                        populateLocations(result, currentLoc, currentTime, lastLogin.getIpAddress(), lastLogin.getCity(),
                                lastLogin.getCountry(), lastLogin.getLatitude(), lastLogin.getLongitude(), lastLoginTime);
                        return result;
                    }
                }
            }
        }

        // Không phát hiện rủi ro
        return DetectionResultDto.allowed(threshold);
    }

    private void populateLocations(
            DetectionResultDto dto,
            GeoLocationDto currentLoc,
            LocalDateTime currentTime,
            String refIp,
            String refCity,
            String refCountry,
            Double refLat,
            Double refLon,
            LocalDateTime refTime) {
        dto.setCurrentIp(currentLoc.getIpAddress());
        dto.setCurrentCity(currentLoc.getCity());
        dto.setCurrentCountry(currentLoc.getCountry());
        dto.setCurrentLatitude(currentLoc.getLatitude());
        dto.setCurrentLongitude(currentLoc.getLongitude());
        dto.setCurrentTime(currentTime);

        dto.setRefIp(refIp);
        dto.setRefCity(refCity);
        dto.setRefCountry(refCountry);
        dto.setRefLatitude(refLat);
        dto.setRefLongitude(refLon);
        dto.setRefTime(refTime);
    }
}
