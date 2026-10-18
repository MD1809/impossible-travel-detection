package com.phenikaa.security.impossibletravel.service;

import com.phenikaa.security.impossibletravel.dto.response.DetectionResultDto;
import com.phenikaa.security.impossibletravel.dto.response.GeoLocationDto;
import com.phenikaa.security.impossibletravel.entity.*;
import com.phenikaa.security.impossibletravel.repository.LoginHistoryRepository;
import com.phenikaa.security.impossibletravel.repository.SystemConfigRepository;
import com.phenikaa.security.impossibletravel.repository.UserSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DisplayName("Kiểm thử ImpossibleTravelEngine: Kịch bản 1 (Concurrent) và Kịch bản 2 (Sequential)")
class ImpossibleTravelEngineTest {

    private UserSessionRepository userSessionRepository;
    private LoginHistoryRepository loginHistoryRepository;
    private SystemConfigRepository systemConfigRepository;
    private ImpossibleTravelEngine engine;

    private static final double HANOI_LAT = 21.0285;
    private static final double HANOI_LON = 105.8542;

    private static final double HCMC_LAT = 10.8231;
    private static final double HCMC_LON = 106.6297;

    private static final double NEW_YORK_LAT = 40.7128;
    private static final double NEW_YORK_LON = -74.0060;

    @BeforeEach
    void setUp() {
        userSessionRepository = Mockito.mock(UserSessionRepository.class);
        loginHistoryRepository = Mockito.mock(LoginHistoryRepository.class);
        systemConfigRepository = Mockito.mock(SystemConfigRepository.class);

        SystemConfig speedConfig = new SystemConfig("MAX_SPEED_THRESHOLD_KMH", "900.0", "Tốc độ tối đa");
        when(systemConfigRepository.findByConfigKey("MAX_SPEED_THRESHOLD_KMH")).thenReturn(Optional.of(speedConfig));

        engine = new ImpossibleTravelEngine(userSessionRepository, loginHistoryRepository, systemConfigRepository);
    }

    @Test
    @DisplayName("Kịch bản 1 (Concurrent): Đang có phiên hoạt động tại Hà Nội, đăng nhập từ New York sau 5 giây -> Chặn CRITICAL")
    void testConcurrentLoginAnomalyBlocked() {
        Long userId = 1L;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fiveSecondsAgo = now.minusSeconds(5);

        UserSession activeSession = new UserSession();
        activeSession.setUserId(userId);
        activeSession.setIpAddress("113.160.224.1");
        activeSession.setCity("Hanoi");
        activeSession.setCountry("Vietnam");
        activeSession.setLatitude(HANOI_LAT);
        activeSession.setLongitude(HANOI_LON);
        activeSession.setLastActivity(fiveSecondsAgo);
        activeSession.setIsActive(true);

        when(userSessionRepository.findByUserIdAndIsActiveTrue(userId))
                .thenReturn(List.of(activeSession));

        GeoLocationDto nyLoc = GeoLocationDto.builder()
                .ipAddress("161.185.160.93")
                .city("New York")
                .country("United States")
                .latitude(NEW_YORK_LAT)
                .longitude(NEW_YORK_LON)
                .resolved(true)
                .build();

        DetectionResultDto result = engine.evaluate(userId, nyLoc, now);

        assertTrue(result.isBlocked(), "Đăng nhập đồng thời tại 2 địa điểm cách nhau 13.000km trong 5s phải bị chặn");
        assertEquals(EventType.CONCURRENT_IMPOSSIBLE_TRAVEL, result.getEventType());
        assertEquals(Severity.CRITICAL, result.getSeverity());
        assertTrue(result.getDistanceKm() > 13000.0);
    }

    @Test
    @DisplayName("Kịch bản 2 (Sequential): Lần đăng nhập trước tại Hà Nội, 5 phút sau đăng nhập tại TP.HCM -> Chặn (13.680 km/h > 900 km/h)")
    void testSequentialImpossibleTravelBlocked() {
        Long userId = 1L;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fiveMinutesAgo = now.minusSeconds(300);

        when(userSessionRepository.findByUserIdAndIsActiveTrue(userId))
                .thenReturn(Collections.emptyList());

        LoginHistory lastLogin = new LoginHistory();
        lastLogin.setUserId(userId);
        lastLogin.setIpAddress("113.160.224.1");
        lastLogin.setCity("Hanoi");
        lastLogin.setCountry("Vietnam");
        lastLogin.setLatitude(HANOI_LAT);
        lastLogin.setLongitude(HANOI_LON);
        lastLogin.setLoginTime(fiveMinutesAgo);
        lastLogin.setStatus(LoginStatus.SUCCESS);

        when(loginHistoryRepository.findFirstByUserIdAndStatusOrderByLoginTimeDesc(userId, LoginStatus.SUCCESS))
                .thenReturn(Optional.of(lastLogin));

        GeoLocationDto hcmcLoc = GeoLocationDto.builder()
                .ipAddress("171.244.10.1")
                .city("Ho Chi Minh City")
                .country("Vietnam")
                .latitude(HCMC_LAT)
                .longitude(HCMC_LON)
                .resolved(true)
                .build();

        DetectionResultDto result = engine.evaluate(userId, hcmcLoc, now);

        assertTrue(result.isBlocked(), "Hà Nội đi TP.HCM trong 5 phút phải bị chặn do vận tốc siêu thanh");
        assertEquals(EventType.SEQUENTIAL_IMPOSSIBLE_TRAVEL, result.getEventType());
        assertTrue(result.getCalculatedSpeedKmh() > 900.0);
    }

    @Test
    @DisplayName("Kịch bản Di chuyển hợp lệ: Hà Nội -> TP.HCM sau 2 giờ bay (~570 km/h < 900 km/h) -> Cho phép (ALLOWED)")
    void testValidFlightTravelAllowed() {
        Long userId = 1L;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoHoursAgo = now.minusSeconds(7200);

        when(userSessionRepository.findByUserIdAndIsActiveTrue(userId))
                .thenReturn(Collections.emptyList());

        LoginHistory lastLogin = new LoginHistory();
        lastLogin.setUserId(userId);
        lastLogin.setIpAddress("113.160.224.1");
        lastLogin.setCity("Hanoi");
        lastLogin.setCountry("Vietnam");
        lastLogin.setLatitude(HANOI_LAT);
        lastLogin.setLongitude(HANOI_LON);
        lastLogin.setLoginTime(twoHoursAgo);
        lastLogin.setStatus(LoginStatus.SUCCESS);

        when(loginHistoryRepository.findFirstByUserIdAndStatusOrderByLoginTimeDesc(userId, LoginStatus.SUCCESS))
                .thenReturn(Optional.of(lastLogin));

        GeoLocationDto hcmcLoc = GeoLocationDto.builder()
                .ipAddress("171.244.10.1")
                .city("Ho Chi Minh City")
                .country("Vietnam")
                .latitude(HCMC_LAT)
                .longitude(HCMC_LON)
                .resolved(true)
                .build();

        DetectionResultDto result = engine.evaluate(userId, hcmcLoc, now);

        assertFalse(result.isBlocked(), "Chuyến bay 2 giờ Hà Nội - TP.HCM là hợp lệ, không bị chặn");
        assertEquals(Severity.LOW, result.getSeverity());
    }

    @Test
    @DisplayName("Kịch bản Di chuyển nội đô gần (< 50 km) -> Bỏ qua kiểm tra tốc độ, cho phép đăng nhập")
    void testShortDistanceTravelAllowed() {
        Long userId = 1L;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tenSecondsAgo = now.minusSeconds(10);

        when(userSessionRepository.findByUserIdAndIsActiveTrue(userId))
                .thenReturn(Collections.emptyList());

        // Điểm A: Trung tâm Hà Nội (Hoàn Kiếm)
        LoginHistory lastLogin = new LoginHistory();
        lastLogin.setUserId(userId);
        lastLogin.setLatitude(21.0285);
        lastLogin.setLongitude(105.8542);
        lastLogin.setLoginTime(tenSecondsAgo);
        lastLogin.setStatus(LoginStatus.SUCCESS);

        when(loginHistoryRepository.findFirstByUserIdAndStatusOrderByLoginTimeDesc(userId, LoginStatus.SUCCESS))
                .thenReturn(Optional.of(lastLogin));

        // Điểm B: Hà Đông (cách ~12 km)
        GeoLocationDto haDongLoc = GeoLocationDto.builder()
                .ipAddress("118.70.1.1")
                .city("Hanoi")
                .country("Vietnam")
                .latitude(20.9720)
                .longitude(105.7770)
                .resolved(true)
                .build();

        DetectionResultDto result = engine.evaluate(userId, haDongLoc, now);

        assertFalse(result.isBlocked(), "Khoảng cách nội đô < 50km không bị coi là Impossible Travel");
    }
}
