package com.phenikaa.security.impossibletravel.service;

import com.phenikaa.security.impossibletravel.dto.DashboardSummaryDto;
import com.phenikaa.security.impossibletravel.dto.SecurityEventDetailDto;
import com.phenikaa.security.impossibletravel.dto.SecurityEventListItemDto;
import com.phenikaa.security.impossibletravel.entity.*;
import com.phenikaa.security.impossibletravel.repository.LoginHistoryRepository;
import com.phenikaa.security.impossibletravel.repository.SecurityEventRepository;
import com.phenikaa.security.impossibletravel.repository.UserRepository;
import com.phenikaa.security.impossibletravel.repository.UserSessionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DashboardService {

    private final LoginHistoryRepository loginHistoryRepository;
    private final UserSessionRepository userSessionRepository;
    private final SecurityEventRepository securityEventRepository;
    private final UserRepository userRepository;

    public DashboardService(
            LoginHistoryRepository loginHistoryRepository,
            UserSessionRepository userSessionRepository,
            SecurityEventRepository securityEventRepository,
            UserRepository userRepository) {
        this.loginHistoryRepository = loginHistoryRepository;
        this.userSessionRepository = userSessionRepository;
        this.securityEventRepository = securityEventRepository;
        this.userRepository = userRepository;
    }

    /**
     * Lấy các chỉ số KPI tổng hợp cho Dashboard
     */
    public DashboardSummaryDto getSummary() {
        long totalLogins = loginHistoryRepository.count();
        long allowedLogins = loginHistoryRepository.countByStatus(LoginStatus.SUCCESS);
        long blockedLogins = loginHistoryRepository.countByStatus(LoginStatus.BLOCKED);
        long activeSessions = userSessionRepository.countByIsActiveTrue();
        long securityAlertsCount = securityEventRepository.count();

        return new DashboardSummaryDto(
                totalLogins,
                allowedLogins,
                blockedLogins,
                activeSessions,
                securityAlertsCount
        );
    }

    /**
     * Lấy danh sách sự kiện an ninh có phân trang và lọc theo mức độ
     */
    public Map<String, Object> getSecurityEvents(int page, int size, String severityStr) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SecurityEvent> eventPage;

        if (severityStr != null && !severityStr.trim().isEmpty()) {
            try {
                Severity severity = Severity.valueOf(severityStr.trim().toUpperCase());
                eventPage = securityEventRepository.findBySeverityOrderByCreatedAtDesc(severity, pageable);
            } catch (IllegalArgumentException e) {
                eventPage = securityEventRepository.findAllByOrderByCreatedAtDesc(pageable);
            }
        } else {
            eventPage = securityEventRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        Map<Long, String> usernameCache = new HashMap<>();

        List<SecurityEventListItemDto> dtos = eventPage.getContent().stream().map(event -> {
            SecurityEventListItemDto dto = new SecurityEventListItemDto();
            dto.setId(event.getId());
            dto.setUserId(event.getUserId());
            dto.setUsername(usernameCache.computeIfAbsent(event.getUserId(), id ->
                    userRepository.findById(id).map(User::getUsername).orElse("unknown")
            ));
            dto.setEventType(event.getEventType());
            dto.setIpAddress(event.getIpAddress());
            dto.setCity(event.getCity());
            dto.setCountry(event.getCountry());
            dto.setRefCity(event.getRefCity());
            dto.setRefCountry(event.getRefCountry());
            dto.setDistanceKm(event.getDistanceKm());
            dto.setCalculatedSpeedKmh(event.getCalculatedSpeedKmh());
            dto.setSeverity(event.getSeverity());
            dto.setActionTaken(event.getActionTaken());
            dto.setCreatedAt(event.getCreatedAt());
            return dto;
        }).toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content", dtos);
        response.put("totalElements", eventPage.getTotalElements());
        response.put("totalPages", eventPage.getTotalPages());
        response.put("currentPage", eventPage.getNumber());
        response.put("pageSize", eventPage.getSize());

        return response;
    }

    /**
     * Lấy chi tiết một sự kiện an ninh kèm số liệu tính toán
     */
    public Optional<SecurityEventDetailDto> getSecurityEventDetail(Long id) {
        return securityEventRepository.findById(id).map(event -> {
            SecurityEventDetailDto detail = new SecurityEventDetailDto();
            detail.setId(event.getId());
            detail.setUserId(event.getUserId());
            detail.setUsername(userRepository.findById(event.getUserId()).map(User::getUsername).orElse("unknown"));
            detail.setEventType(event.getEventType());
            detail.setSeverity(event.getSeverity());
            detail.setActionTaken(event.getActionTaken());
            detail.setCreatedAt(event.getCreatedAt());

            SecurityEventDetailDto.LocationDetail current = new SecurityEventDetailDto.LocationDetail(
                    event.getIpAddress(),
                    event.getCity(),
                    event.getCountry(),
                    event.getLatitude(),
                    event.getLongitude(),
                    event.getCreatedAt()
            );
            detail.setCurrentAttempt(current);

            SecurityEventDetailDto.LocationDetail ref = new SecurityEventDetailDto.LocationDetail(
                    "N/A",
                    event.getRefCity(),
                    event.getRefCountry(),
                    event.getRefLatitude(),
                    event.getRefLongitude(),
                    event.getRefTime()
            );
            detail.setReferenceAttempt(ref);

            long timeDiffSeconds = event.getTimeDiffMinutes() != null
                    ? (long) (event.getTimeDiffMinutes() * 60.0)
                    : 0;

            SecurityEventDetailDto.MetricsDetail metrics = new SecurityEventDetailDto.MetricsDetail(
                    event.getDistanceKm(),
                    timeDiffSeconds,
                    event.getTimeDiffMinutes(),
                    event.getCalculatedSpeedKmh(),
                    event.getThresholdSpeedKmh()
            );
            detail.setMetrics(metrics);

            return detail;
        });
    }

    /**
     * Lấy toàn bộ lịch sử đăng nhập
     */
    public List<Map<String, Object>> getLoginHistory(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        Page<LoginHistory> page = loginHistoryRepository.findAllByOrderByLoginTimeDesc(pageable);
        Map<Long, String> usernameCache = new HashMap<>();

        return page.getContent().stream().map(h -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", h.getId());
            map.put("userId", h.getUserId());

            String resolvedUsername = h.getAttemptedUsername();
            if ((resolvedUsername == null || resolvedUsername.isEmpty()) && h.getUserId() != null) {
                resolvedUsername = usernameCache.computeIfAbsent(h.getUserId(), id ->
                        userRepository.findById(id).map(User::getUsername).orElse("unknown")
                );
            }
            map.put("username", resolvedUsername != null ? resolvedUsername : "unknown");
            map.put("ipAddress", h.getIpAddress());
            map.put("city", h.getCity());
            map.put("country", h.getCountry());
            map.put("latitude", h.getLatitude());
            map.put("longitude", h.getLongitude());
            map.put("status", h.getStatus().name());
            map.put("failureReason", h.getFailureReason());
            map.put("loginTime", h.getLoginTime());
            return map;
        }).toList();
    }

    /**
     * Lấy danh sách các phiên đang hoạt động
     */
    public List<Map<String, Object>> getActiveSessions() {
        List<UserSession> sessions = userSessionRepository.findByIsActiveTrue();
        Map<Long, String> usernameCache = new HashMap<>();

        return sessions.stream().map(s -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", s.getId());
            map.put("userId", s.getUserId());
            map.put("username", usernameCache.computeIfAbsent(s.getUserId(), id ->
                    userRepository.findById(id).map(User::getUsername).orElse("unknown")
            ));
            map.put("sessionToken", s.getSessionToken());
            map.put("ipAddress", s.getIpAddress());
            map.put("city", s.getCity());
            map.put("country", s.getCountry());
            map.put("latitude", s.getLatitude());
            map.put("longitude", s.getLongitude());
            map.put("loginTime", s.getLoginTime());
            map.put("lastActivity", s.getLastActivity());
            map.put("expiresAt", s.getExpiresAt());
            return map;
        }).toList();
    }
}
