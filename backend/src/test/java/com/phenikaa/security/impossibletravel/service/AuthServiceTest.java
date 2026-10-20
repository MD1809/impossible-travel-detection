package com.phenikaa.security.impossibletravel.service;

import com.phenikaa.security.impossibletravel.dto.request.*;
import com.phenikaa.security.impossibletravel.dto.response.*;
import com.phenikaa.security.impossibletravel.entity.*;
import com.phenikaa.security.impossibletravel.repository.LoginHistoryRepository;
import com.phenikaa.security.impossibletravel.repository.SecurityEventRepository;
import com.phenikaa.security.impossibletravel.repository.UserRepository;
import com.phenikaa.security.impossibletravel.repository.UserSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử AuthService: Đăng ký, Đăng nhập kết hợp Động cơ phát hiện, và Đăng xuất")
class AuthServiceTest {

    private UserRepository userRepository;
    private UserSessionRepository userSessionRepository;
    private LoginHistoryRepository loginHistoryRepository;
    private SecurityEventRepository securityEventRepository;
    private GeoLocationService geoLocationService;
    private ImpossibleTravelEngine impossibleTravelEngine;
    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        userSessionRepository = Mockito.mock(UserSessionRepository.class);
        loginHistoryRepository = Mockito.mock(LoginHistoryRepository.class);
        securityEventRepository = Mockito.mock(SecurityEventRepository.class);
        geoLocationService = Mockito.mock(GeoLocationService.class);
        impossibleTravelEngine = Mockito.mock(ImpossibleTravelEngine.class);
        passwordEncoder = new BCryptPasswordEncoder();

        when(securityEventRepository.save(any(SecurityEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService = new AuthService(
                userRepository,
                userSessionRepository,
                loginHistoryRepository,
                securityEventRepository,
                geoLocationService,
                impossibleTravelEngine,
                passwordEncoder,
                Mockito.mock(EmailOtpService.class)
        );
    }

    @Test
    @DisplayName("Đăng ký thành công người dùng mới")
    void testRegisterSuccess() {
        RegisterRequestDto request = new RegisterRequestDto("student01", "Password@123", "Nguyen Van A");
        request.setEmail("student01@example.test");
        when(userRepository.existsByUsername("student01")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        // Registration now returns a pending email-verification challenge.
        EmailOtpService otp = Mockito.mock(EmailOtpService.class);
        var pending = new LoginResponseDto();
        pending.setVerificationRequired(true);
        when(otp.issue(any(), eq("EMAIL_VERIFY"), isNull(), isNull())).thenReturn(pending);
        authService = new AuthService(userRepository, userSessionRepository, loginHistoryRepository,
            securityEventRepository, geoLocationService, impossibleTravelEngine, passwordEncoder, otp);
        RegisterResponseDto response = authService.register(request);

        assertTrue(response.isSuccess());
        assertEquals(1L, response.getUserId());
        assertEquals("student01", response.getUsername());
    }

    @Test
    @DisplayName("Đăng nhập thành công khi thông tin đúng và không vi phạm Impossible Travel")
    void testLoginSuccess() {
        User user = new User();
        user.setId(1L);
        user.setUsername("student01");
        user.setPasswordHash(passwordEncoder.encode("Password@123"));
        user.setRole(Role.ROLE_USER);

        when(userRepository.findByUsername("student01")).thenReturn(Optional.of(user));

        GeoLocationDto hanoiLoc = GeoLocationDto.builder()
                .ipAddress("113.160.224.1")
                .city("Hanoi")
                .country("Vietnam")
                .latitude(21.0285)
                .longitude(105.8542)
                .resolved(true)
                .build();
        when(geoLocationService.resolve("113.160.224.1")).thenReturn(hanoiLoc);

        when(impossibleTravelEngine.evaluate(eq(1L), eq(hanoiLoc), any(LocalDateTime.class)))
                .thenReturn(DetectionResultDto.allowed(900.0));

        LoginRequestDto request = new LoginRequestDto("student01", "Password@123", "113.160.224.1");
        LoginResponseDto response = authService.login(request, "113.160.224.1");

        assertTrue(response.isSuccess());
        assertEquals(200, response.getStatus());
        assertNotNull(response.getSessionToken());
        assertTrue(response.getSessionToken().startsWith("sess_"));
        verify(userSessionRepository, times(1)).save(any(UserSession.class));
        verify(loginHistoryRepository, times(1)).save(any(LoginHistory.class));
    }

    @Test
    @DisplayName("Đăng nhập thất bại do sai mật khẩu -> 401 Unauthorized và lưu lịch sử lỗi")
    void testLoginWrongPassword() {
        User user = new User();
        user.setId(1L);
        user.setUsername("student01");
        user.setPasswordHash(passwordEncoder.encode("Password@123"));

        when(userRepository.findByUsername("student01")).thenReturn(Optional.of(user));
        when(geoLocationService.resolve(anyString())).thenReturn(GeoLocationDto.builder().ipAddress("113.160.224.1").build());

        LoginRequestDto request = new LoginRequestDto("student01", "WrongPassword", "113.160.224.1");
        LoginResponseDto response = authService.login(request, "113.160.224.1");

        assertFalse(response.isSuccess());
        assertEquals(401, response.getStatus());
        verify(loginHistoryRepository, times(1)).save(any(LoginHistory.class));
        verify(userSessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Đăng nhập bị chặn do Impossible Travel -> 403 Forbidden, ghi log security_events và không cấp phiên")
    void testLoginBlockedByImpossibleTravel() {
        User user = new User();
        user.setId(1L);
        user.setUsername("student01");
        user.setPasswordHash(passwordEncoder.encode("Password@123"));
        user.setRole(Role.ROLE_USER);

        when(userRepository.findByUsername("student01")).thenReturn(Optional.of(user));

        GeoLocationDto nyLoc = GeoLocationDto.builder()
                .ipAddress("161.185.160.93")
                .city("New York")
                .country("United States")
                .latitude(40.7128)
                .longitude(-74.0060)
                .resolved(true)
                .build();
        when(geoLocationService.resolve("161.185.160.93")).thenReturn(nyLoc);

        DetectionResultDto blockedResult = DetectionResultDto.blocked(
                EventType.CONCURRENT_IMPOSSIBLE_TRAVEL,
                Severity.CRITICAL,
                13125.40,
                5,
                9488240.00,
                900.0,
                "Phát hiện đăng nhập tức thời tại New York khi đang có phiên tại Hà Nội"
        );
        blockedResult.setRefCity("Hanoi");
        blockedResult.setRefCountry("Vietnam");
        blockedResult.setRefLatitude(21.0285);
        blockedResult.setRefLongitude(105.8542);
        blockedResult.setRefIp("113.160.224.1");
        blockedResult.setRefTime(LocalDateTime.now().minusSeconds(5));

        when(impossibleTravelEngine.evaluate(eq(1L), eq(nyLoc), any(LocalDateTime.class)))
                .thenReturn(blockedResult);

        LoginRequestDto request = new LoginRequestDto("student01", "Password@123", "161.185.160.93");
        LoginResponseDto response = authService.login(request, "161.185.160.93");

        assertFalse(response.isSuccess());
        assertEquals(403, response.getStatus());
        assertEquals("Forbidden", response.getError());
        assertNotNull(response.getDetectionDetails());
        assertEquals("CONCURRENT_IMPOSSIBLE_TRAVEL", response.getDetectionDetails().get("ruleTriggered"));

        // Xác nhận đã lưu sự kiện an ninh
        verify(securityEventRepository, times(1)).save(any(SecurityEvent.class));
        // Xác nhận không tạo phiên làm việc mới
        verify(userSessionRepository, never()).save(any(UserSession.class));
    }

    @Test
    @DisplayName("Đăng xuất hủy phiên làm việc thành công")
    void testLogoutSuccess() {
        UserSession session = new UserSession();
        session.setSessionToken("sess_12345");
        session.setIsActive(true);

        when(userSessionRepository.findBySessionToken("sess_12345")).thenReturn(Optional.of(session));

        boolean result = authService.logout("sess_12345");

        assertTrue(result);
        assertFalse(session.getIsActive());
        verify(userSessionRepository, times(1)).save(session);
    }
}
