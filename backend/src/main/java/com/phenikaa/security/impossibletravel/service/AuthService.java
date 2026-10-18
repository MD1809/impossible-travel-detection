package com.phenikaa.security.impossibletravel.service;

import com.phenikaa.security.impossibletravel.dto.request.*;
import com.phenikaa.security.impossibletravel.dto.response.*;
import com.phenikaa.security.impossibletravel.entity.*;
import com.phenikaa.security.impossibletravel.repository.LoginHistoryRepository;
import com.phenikaa.security.impossibletravel.repository.SecurityEventRepository;
import com.phenikaa.security.impossibletravel.repository.UserRepository;
import com.phenikaa.security.impossibletravel.repository.UserSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final SecurityEventRepository securityEventRepository;
    private final GeoLocationService geoLocationService;
    private final ImpossibleTravelEngine impossibleTravelEngine;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            UserSessionRepository userSessionRepository,
            LoginHistoryRepository loginHistoryRepository,
            SecurityEventRepository securityEventRepository,
            GeoLocationService geoLocationService,
            ImpossibleTravelEngine impossibleTravelEngine,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userSessionRepository = userSessionRepository;
        this.loginHistoryRepository = loginHistoryRepository;
        this.securityEventRepository = securityEventRepository;
        this.geoLocationService = geoLocationService;
        this.impossibleTravelEngine = impossibleTravelEngine;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Đăng ký tài khoản người dùng mới với mật khẩu băm BCrypt
     */
    @Transactional
    public RegisterResponseDto register(RegisterRequestDto request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return RegisterResponseDto.error("Tên đăng nhập không được để trống");
        }
        if (request.getPassword() == null || request.getPassword().trim().length() < 6) {
            return RegisterResponseDto.error("Mật khẩu phải có độ dài tối thiểu 6 ký tự");
        }

        String username = request.getUsername().trim();
        if (userRepository.existsByUsername(username)) {
            return RegisterResponseDto.error("Tên đăng nhập đã tồn tại trong hệ thống");
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName() != null ? request.getFullName().trim() : username);
        user.setRole(Role.ROLE_USER);
        user.setIsActive(true);

        User savedUser = userRepository.save(user);
        log.info("Đăng ký thành công người dùng mới: id={}, username={}", savedUser.getId(), savedUser.getUsername());

        return RegisterResponseDto.success(savedUser.getId(), savedUser.getUsername());
    }

    /**
     * Xác thực đăng nhập, tích hợp phân giải GeoIP và Động cơ phát hiện Impossible Travel
     */
    @Transactional
    public LoginResponseDto login(LoginRequestDto request, String remoteIp) {
        LocalDateTime now = LocalDateTime.now();
        if (request.getLoginTime() != null && !request.getLoginTime().trim().isEmpty()) {
            try {
                now = LocalDateTime.parse(request.getLoginTime().trim());
            } catch (Exception e) {
                log.warn("Không thể phân tích loginTime tuỳ chọn: {}, sử dụng thời gian hiện tại", request.getLoginTime());
            }
        }
        String effectiveIp = (request.getIpAddress() != null && !request.getIpAddress().trim().isEmpty())
                ? request.getIpAddress().trim()
                : (remoteIp != null && !remoteIp.isEmpty() ? remoteIp : "127.0.0.1");

        // 1. Phân giải địa chỉ IP ra tọa độ địa lý
        GeoLocationDto location = geoLocationService.resolve(effectiveIp);

        // 2. Tìm kiếm người dùng
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        if (userOpt.isEmpty()) {
            saveLoginHistory(null, request.getUsername(), effectiveIp, location, now, LoginStatus.FAILED, "Tài khoản không tồn tại");
            return LoginResponseDto.unauthorized("Tên đăng nhập hoặc mật khẩu không chính xác");
        }
        if (!passwordEncoder.matches(request.getPassword(), userOpt.get().getPasswordHash())) {
            saveLoginHistory(userOpt.get().getId(), userOpt.get().getUsername(), effectiveIp, location, now, LoginStatus.FAILED, "Mật khẩu không chính xác");
            return LoginResponseDto.unauthorized("Tên đăng nhập hoặc mật khẩu không chính xác");
        }

        User user = userOpt.get();

        // 3. Thực thi Động cơ Impossible Travel
        DetectionResultDto detection = impossibleTravelEngine.evaluate(user.getId(), location, now);

        if (detection.isBlocked()) {
            // Lưu sự kiện an ninh vào bảng security_events
            SecurityEvent event = new SecurityEvent();
            event.setUserId(user.getId());
            event.setEventType(detection.getEventType().name());
            event.setIpAddress(effectiveIp);
            event.setCity(location.getCity());
            event.setCountry(location.getCountry());
            event.setLatitude(location.getLatitude());
            event.setLongitude(location.getLongitude());
            event.setRefCity(detection.getRefCity());
            event.setRefCountry(detection.getRefCountry());
            event.setRefLatitude(detection.getRefLatitude());
            event.setRefLongitude(detection.getRefLongitude());
            event.setRefIp(detection.getRefIp());
            event.setRefTime(detection.getRefTime());
            event.setDistanceKm(detection.getDistanceKm());
            event.setTimeDiffMinutes(detection.getTimeDiffMinutes());
            Double safeSpeed = detection.getCalculatedSpeedKmh();
            if (safeSpeed != null && (Double.isInfinite(safeSpeed) || Double.isNaN(safeSpeed) || safeSpeed > 99999999.0)) {
                safeSpeed = 99999999.0;
            }
            event.setCalculatedSpeedKmh(safeSpeed);
            event.setThresholdSpeedKmh(detection.getThresholdSpeedKmh());
            event.setSeverity(detection.getSeverity());
            event.setActionTaken("BLOCKED");
            SecurityEvent savedEvent = securityEventRepository.save(event);

            // Xác định thông tin khi bị chặn trong Nhật ký hoạt động:
            // Kịch bản 1: Phát hiện phiên đăng nhập đồng thời bất thường của tài khoản
            // Kịch bản 2: Phát hiện phiên đăng nhập bất khả thi về khoảng cách trong thời gian ngắn
            String blockedReason = (detection.getEventType() == EventType.CONCURRENT_IMPOSSIBLE_TRAVEL)
                    ? "Phát hiện phiên đăng nhập đồng thời bất thường của tài khoản"
                    : "Phát hiện phiên đăng nhập bất khả thi về khoảng cách trong thời gian ngắn";

            // Ghi nhận Lịch sử đăng nhập bị chặn kèm ID của sự kiện an ninh
            Long savedEventId = (savedEvent != null) ? savedEvent.getId() : null;
            saveLoginHistory(user.getId(), user.getUsername(), effectiveIp, location, now, LoginStatus.BLOCKED, blockedReason, savedEventId);

            // Đóng gói Detection Details theo chuẩn 05_API_SPECIFICATION.md
            Map<String, Object> details = new LinkedHashMap<>();
            details.put("ruleTriggered", detection.getEventType().name());

            Map<String, Object> currentLoginMap = new LinkedHashMap<>();
            currentLoginMap.put("city", location.getCity() != null ? location.getCity() : "Unknown");
            currentLoginMap.put("country", location.getCountry() != null ? location.getCountry() : "Unknown");
            currentLoginMap.put("coordinates", Arrays.asList(location.getLatitude(), location.getLongitude()));
            currentLoginMap.put("ipAddress", effectiveIp);
            currentLoginMap.put("time", now.toString());
            details.put("currentLogin", currentLoginMap);

            Map<String, Object> refLoginMap = new LinkedHashMap<>();
            refLoginMap.put("city", detection.getRefCity() != null ? detection.getRefCity() : "Unknown");
            refLoginMap.put("country", detection.getRefCountry() != null ? detection.getRefCountry() : "Unknown");
            refLoginMap.put("coordinates", Arrays.asList(
                    detection.getRefLatitude() != null ? detection.getRefLatitude() : 0.0,
                    detection.getRefLongitude() != null ? detection.getRefLongitude() : 0.0
            ));
            refLoginMap.put("ipAddress", detection.getRefIp() != null ? detection.getRefIp() : "Unknown");
            refLoginMap.put("time", detection.getRefTime() != null ? detection.getRefTime().toString() : now.toString());
            details.put("referenceLogin", refLoginMap);

            details.put("distanceKm", detection.getDistanceKm());
            details.put("timeDifferenceMinutes", detection.getTimeDiffMinutes());
            details.put("calculatedSpeedKmh", safeSpeed);
            details.put("thresholdSpeedKmh", detection.getThresholdSpeedKmh());
            details.put("action", "BLOCKED");

            return LoginResponseDto.forbidden(
                    "Cảnh báo an ninh: Phát hiện di chuyển bất khả thi (Impossible Travel Detected)", details);
        }

        // 4. Đăng nhập thành công hợp lệ
        saveLoginHistory(user.getId(), user.getUsername(), effectiveIp, location, now, LoginStatus.SUCCESS, null);

        // Tạo phiên làm việc mới trong user_sessions
        String sessionToken = "sess_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        UserSession session = new UserSession();
        session.setUserId(user.getId());
        session.setSessionToken(sessionToken);
        session.setIpAddress(effectiveIp);
        session.setCity(location.getCity());
        session.setCountry(location.getCountry());
        session.setLatitude(location.getLatitude());
        session.setLongitude(location.getLongitude());
        session.setLoginTime(now);
        session.setLastActivity(now);
        session.setExpiresAt(now.plusDays(1));
        session.setIsActive(true);
        userSessionRepository.save(session);

        LoginResponseDto.UserInfoDto userInfo = new LoginResponseDto.UserInfoDto(
                user.getId(), user.getUsername(), user.getRole().name()
        );
        LoginResponseDto.LocationDto locDto = new LoginResponseDto.LocationDto(
                effectiveIp, location.getCity(), location.getCountry(), location.getLatitude(), location.getLongitude()
        );

        return LoginResponseDto.success(sessionToken, userInfo, locDto);
    }

    /**
     * Đăng xuất và hủy kích hoạt phiên làm việc
     */
    @Transactional
    public boolean logout(String sessionToken) {
        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            return false;
        }
        Optional<UserSession> sessionOpt = userSessionRepository.findBySessionToken(sessionToken.trim());
        if (sessionOpt.isPresent()) {
            UserSession session = sessionOpt.get();
            session.setIsActive(false);
            userSessionRepository.save(session);

            String username = userRepository.findById(session.getUserId()).map(User::getUsername).orElse(null);
            GeoLocationDto loc = GeoLocationDto.builder()
                    .ipAddress(session.getIpAddress())
                    .city(session.getCity())
                    .country(session.getCountry())
                    .latitude(session.getLatitude())
                    .longitude(session.getLongitude())
                    .resolved(true)
                    .build();
            saveLoginHistory(session.getUserId(), username, session.getIpAddress(), loc, LocalDateTime.now(), LoginStatus.LOGOUT, "Đăng xuất thành công");

            log.info("Phiên làm việc đã bị hủy: token={}", sessionToken);
            return true;
        }
        return false;
    }

    /**
     * Đăng xuất toàn bộ các phiên làm việc đang hoạt động của người dùng
     */
    @Transactional
    public int logoutAllUserSessions(String username) {
        if (username == null || username.trim().isEmpty()) {
            return 0;
        }
        Optional<User> userOpt = userRepository.findByUsername(username.trim());
        if (userOpt.isEmpty()) {
            return 0;
        }
        User user = userOpt.get();
        List<UserSession> activeSessions = userSessionRepository.findByUserIdAndIsActiveTrue(user.getId());
        LocalDateTime now = LocalDateTime.now();
        for (UserSession session : activeSessions) {
            session.setIsActive(false);
            GeoLocationDto loc = GeoLocationDto.builder()
                    .ipAddress(session.getIpAddress())
                    .city(session.getCity())
                    .country(session.getCountry())
                    .latitude(session.getLatitude())
                    .longitude(session.getLongitude())
                    .resolved(true)
                    .build();
            saveLoginHistory(user.getId(), user.getUsername(), session.getIpAddress(), loc, now, LoginStatus.LOGOUT, "Đăng xuất bởi quản trị viên");
        }
        userSessionRepository.saveAll(activeSessions);
        log.info("Đã đóng {} phiên làm việc của user: {}", activeSessions.size(), username);
        return activeSessions.size();
    }

    private void saveLoginHistory(
            Long userId,
            String attemptedUsername,
            String ip,
            GeoLocationDto loc,
            LocalDateTime time,
            LoginStatus status,
            String failureReason) {
        saveLoginHistory(userId, attemptedUsername, ip, loc, time, status, failureReason, null);
    }

    private void saveLoginHistory(
            Long userId,
            String attemptedUsername,
            String ip,
            GeoLocationDto loc,
            LocalDateTime time,
            LoginStatus status,
            String failureReason,
            Long securityEventId) {
        LoginHistory history = new LoginHistory();
        history.setUserId(userId);
        history.setAttemptedUsername(attemptedUsername);
        history.setIpAddress(ip);
        if (loc != null) {
            history.setCity(loc.getCity());
            history.setCountry(loc.getCountry());
            history.setLatitude(loc.getLatitude());
            history.setLongitude(loc.getLongitude());
        }
        history.setLoginTime(time);
        history.setStatus(status);
        history.setFailureReason(failureReason);
        history.setSecurityEventId(securityEventId);
        loginHistoryRepository.save(history);
    }
}
