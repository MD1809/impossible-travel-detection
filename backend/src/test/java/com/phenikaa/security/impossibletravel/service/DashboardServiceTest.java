package com.phenikaa.security.impossibletravel.service;

import com.phenikaa.security.impossibletravel.dto.DashboardSummaryDto;
import com.phenikaa.security.impossibletravel.dto.SecurityEventDetailDto;
import com.phenikaa.security.impossibletravel.entity.*;
import com.phenikaa.security.impossibletravel.repository.LoginHistoryRepository;
import com.phenikaa.security.impossibletravel.repository.SecurityEventRepository;
import com.phenikaa.security.impossibletravel.repository.UserRepository;
import com.phenikaa.security.impossibletravel.repository.UserSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("Kiểm thử DashboardService: Chỉ số thống kê KPI và Sự kiện an ninh")
class DashboardServiceTest {

    private LoginHistoryRepository loginHistoryRepository;
    private UserSessionRepository userSessionRepository;
    private SecurityEventRepository securityEventRepository;
    private UserRepository userRepository;
    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        loginHistoryRepository = Mockito.mock(LoginHistoryRepository.class);
        userSessionRepository = Mockito.mock(UserSessionRepository.class);
        securityEventRepository = Mockito.mock(SecurityEventRepository.class);
        userRepository = Mockito.mock(UserRepository.class);

        dashboardService = new DashboardService(
                loginHistoryRepository,
                userSessionRepository,
                securityEventRepository,
                userRepository
        );
    }

    @Test
    @DisplayName("Thống kê KPI tổng quan Dashboard trả về đầy đủ các chỉ số")
    void testGetSummary() {
        when(loginHistoryRepository.count()).thenReturn(150L);
        when(loginHistoryRepository.countByStatus(LoginStatus.SUCCESS)).thenReturn(130L);
        when(loginHistoryRepository.countByStatus(LoginStatus.BLOCKED)).thenReturn(20L);
        when(userSessionRepository.countByIsActiveTrue()).thenReturn(8L);
        when(securityEventRepository.count()).thenReturn(20L);

        DashboardSummaryDto summary = dashboardService.getSummary();

        assertEquals(150L, summary.getTotalLogins());
        assertEquals(130L, summary.getAllowedLogins());
        assertEquals(20L, summary.getBlockedLogins());
        assertEquals(8L, summary.getActiveSessions());
        assertEquals(20L, summary.getSecurityAlertsCount());
    }

    @Test
    @DisplayName("Lấy danh sách sự kiện an ninh có phân trang")
    void testGetSecurityEvents() {
        SecurityEvent event = new SecurityEvent();
        event.setId(1L);
        event.setUserId(10L);
        event.setEventType("CONCURRENT_IMPOSSIBLE_TRAVEL");
        event.setIpAddress("161.185.160.93");
        event.setCity("New York");
        event.setCountry("United States");
        event.setSeverity(Severity.CRITICAL);
        event.setActionTaken("BLOCKED");
        event.setCreatedAt(LocalDateTime.now());

        when(securityEventRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(event)));

        User user = new User();
        user.setId(10L);
        user.setUsername("tester01");
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        Map<String, Object> result = dashboardService.getSecurityEvents(0, 10, null);

        assertNotNull(result);
        assertEquals(1L, result.get("totalElements"));
        List<?> content = (List<?>) result.get("content");
        assertEquals(1, content.size());
    }

    @Test
    @DisplayName("Lấy chi tiết sự kiện an ninh theo ID")
    void testGetSecurityEventDetail() {
        SecurityEvent event = new SecurityEvent();
        event.setId(1L);
        event.setUserId(10L);
        event.setEventType("CONCURRENT_IMPOSSIBLE_TRAVEL");
        event.setIpAddress("161.185.160.93");
        event.setCity("New York");
        event.setCountry("United States");
        event.setLatitude(40.7128);
        event.setLongitude(-74.0060);
        event.setRefCity("Hanoi");
        event.setRefCountry("Vietnam");
        event.setRefLatitude(21.0285);
        event.setRefLongitude(105.8542);
        event.setDistanceKm(13125.40);
        event.setTimeDiffMinutes(0.08);
        event.setCalculatedSpeedKmh(9488240.00);
        event.setThresholdSpeedKmh(900.00);
        event.setSeverity(Severity.CRITICAL);
        event.setActionTaken("BLOCKED");
        event.setCreatedAt(LocalDateTime.now());

        when(securityEventRepository.findById(1L)).thenReturn(Optional.of(event));

        User user = new User();
        user.setId(10L);
        user.setUsername("tester01");
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        Optional<SecurityEventDetailDto> detailOpt = dashboardService.getSecurityEventDetail(1L);

        assertTrue(detailOpt.isPresent());
        SecurityEventDetailDto detail = detailOpt.get();
        assertEquals("CONCURRENT_IMPOSSIBLE_TRAVEL", detail.getEventType());
        assertEquals("tester01", detail.getUsername());
        assertNotNull(detail.getCurrentAttempt());
        assertEquals("New York", detail.getCurrentAttempt().getCity());
        assertNotNull(detail.getReferenceAttempt());
        assertEquals("Hanoi", detail.getReferenceAttempt().getCity());
        assertEquals(13125.40, detail.getMetrics().getDistanceKm());
    }
}
