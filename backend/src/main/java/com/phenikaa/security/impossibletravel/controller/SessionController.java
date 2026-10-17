package com.phenikaa.security.impossibletravel.controller;

import com.phenikaa.security.impossibletravel.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final DashboardService dashboardService;

    public SessionController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * API Lấy danh sách các phiên làm việc đang hoạt động
     * GET /api/sessions/active
     */
    @GetMapping("/active")
    public ResponseEntity<List<Map<String, Object>>> getActiveSessions() {
        return ResponseEntity.ok(dashboardService.getActiveSessions());
    }
}
