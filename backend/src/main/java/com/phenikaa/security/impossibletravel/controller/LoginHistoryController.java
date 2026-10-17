package com.phenikaa.security.impossibletravel.controller;

import com.phenikaa.security.impossibletravel.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/login-history")
public class LoginHistoryController {

    private final DashboardService dashboardService;

    public LoginHistoryController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * API Lấy danh sách lịch sử đăng nhập
     * GET /api/login-history?limit=100
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getLoginHistory(
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(dashboardService.getLoginHistory(limit));
    }
}
