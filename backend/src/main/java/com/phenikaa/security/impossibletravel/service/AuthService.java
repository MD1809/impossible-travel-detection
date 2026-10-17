package com.phenikaa.security.impossibletravel.service;

import com.phenikaa.security.impossibletravel.dto.*;
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
        String effectiveIp = (request.getIpAddress() != null && !request.getIpAddress().trim().isEmpty())
                ? request.getIpAddress().trim()
                : (remoteIp != null && !remoteIp.isEmpty() ? remoteIp : "127.0.0.1");

        // 1. Phân giải địa chỉ IP ra tọa độ địa lý
        GeoLocationDto location = geoLocationService.resolve(effectiveIp);

        // 2. Tìm kiếm người dùng
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        if (userOpt.isEmpty() || !passwordEncoder.matches(request.getPassword(), userOpt.get().getPasswordHash())) {
            if (userOpt.isPresent()) {
                saveLoginHistory(userOpt.get().getId(), effectiveIp, location, now, LoginStatus.FAILED, "Sai mật khẩu");
            }
            return LoginResponseDto.unauthorized("Tên đăng nhập hoặc mật khẩu không chính xác");
        }

        User user = userOpt.get();

        // 3. Thực thi Động cơ Impossible Travel
        DetectionResultDto detection = impossibleTravelEngine.evaluate(user.getId(), location, now);

        if (detection.isBlocked()) {
            // Ghi nhận Lịch sử đăng nhập bị chặn
            saveLoginHistory(user.getId(), effectiveIp, location, now, LoginStatus.BLOCKED, detection.getReason());

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
            event.setRefTime(detection.getRefTime());
            event.setDistanceKm(detection.getDistanceKm());
            event.setTimeDiffMinutes(detection.getTimeDiffMinutes());
            event.setCalculatedSpeedKmh(detection.getCalculatedSpeedKmh());
            event.setThresholdSpeedKmh(detection.getThresholdSpeedKmh());
            event.setSeverity(detection.getSeverity());
            event.setActionTaken("BLOCKED");
            securityEventRepository.save(event);

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
            details.put("calculatedSpeedKmh", detection.getCalculatedSpeedKmh());
            details.put("thresholdSpeedKmh", detection.getThresholdSpeedKmh());
            details.put("action", "BLOCKED");

            return LoginResponseDto.forbidden(
                    "Cảnh báo an ninh: Phát hiện di chuyển bất khả thi (Impossible Travel Detected)", details);
        }

        // 4. Đăng nhập thành công hợp lệ
        saveLoginHistory(user.getId(), effectiveIp, location, now, LoginStatus.SUCCESS, null);

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
            log.info("Phiên làm việc đã bị hủy: token={}", sessionToken);
            return true;
        }
        return false;
    }

    private void saveLoginHistory(
            Long userId,
            String ip,
            GeoLocationDto loc,
            LocalDateTime time,
            LoginStatus status,
            String failureReason) {
        LoginHistory history = new LoginHistory();
        history.setUserId(userId);
        history.setIpAddress(ip);
        history.setCity(loc.getCity());
        history.setCountry(loc.getCountry());
        history.setLatitude(loc.getLatitude());
        history.setLongitude(loc.getLongitude());
        history.setLoginTime(time);
        history.setStatus(status);
        history.setFailureReason(failureReason);
        loginHistoryRepository.save(history);
    }
}
