package com.phenikaa.security.impossibletravel.controller;

import com.phenikaa.security.impossibletravel.dto.request.LoginRequestDto;
import com.phenikaa.security.impossibletravel.dto.response.LoginResponseDto;
import com.phenikaa.security.impossibletravel.dto.request.RegisterRequestDto;
import com.phenikaa.security.impossibletravel.dto.response.RegisterResponseDto;
import com.phenikaa.security.impossibletravel.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * API Đăng ký tài khoản người dùng
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> register(@RequestBody RegisterRequestDto request) {
        RegisterResponseDto response = authService.register(request);
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * API Đăng nhập và Đánh giá rủi ro Impossible Travel
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @RequestBody LoginRequestDto request,
            HttpServletRequest httpRequest) {
        String remoteIp = httpRequest.getHeader("X-Forwarded-For");
        if (remoteIp == null || remoteIp.isEmpty() || "unknown".equalsIgnoreCase(remoteIp)) {
            remoteIp = httpRequest.getRemoteAddr();
        } else if (remoteIp.contains(",")) {
            remoteIp = remoteIp.split(",")[0].trim();
        }

        LoginResponseDto response = authService.login(request, remoteIp);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else if (response.getStatus() != null && response.getStatus() == 403) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * API Đăng xuất phiên làm việc
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7).trim();
        }

        boolean success = authService.logout(token);
        if (success) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã kết thúc phiên làm việc thành công"
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Phiên làm việc đã kết thúc hoặc không tồn tại"
            ));
        }
    }

    /**
     * API Đăng xuất toàn bộ các phiên làm việc của người dùng
     * POST /api/auth/logout-all
     */
    @PostMapping("/logout-all")
    public ResponseEntity<Map<String, Object>> logoutAll(
            @RequestParam(required = false) String username,
            @RequestBody(required = false) Map<String, String> body) {
        String targetUser = username;
        if ((targetUser == null || targetUser.isEmpty()) && body != null) {
            targetUser = body.get("username");
        }
        int count = authService.logoutAllUserSessions(targetUser);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã kết thúc " + count + " phiên làm việc của tài khoản " + (targetUser != null ? targetUser : ""),
                "closedSessionsCount", count
        ));
    }
}
