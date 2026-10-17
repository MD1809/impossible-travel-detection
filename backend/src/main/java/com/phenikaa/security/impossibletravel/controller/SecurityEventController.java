package com.phenikaa.security.impossibletravel.controller;

import com.phenikaa.security.impossibletravel.dto.SecurityEventDetailDto;
import com.phenikaa.security.impossibletravel.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/security-events")
public class SecurityEventController {

    private final DashboardService dashboardService;

    public SecurityEventController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * API Lấy danh sách sự kiện an ninh
     * GET /api/security-events?page=0&size=10&severity=CRITICAL
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getSecurityEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String severity) {
        return ResponseEntity.ok(dashboardService.getSecurityEvents(page, size, severity));
    }

    /**
     * API Lấy chi tiết một sự kiện an ninh
     * GET /api/security-events/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<SecurityEventDetailDto> getSecurityEventDetail(@PathVariable Long id) {
        return dashboardService.getSecurityEventDetail(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
