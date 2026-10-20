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
    private final com.phenikaa.security.impossibletravel.service.EmailOtpService emailOtpService;

    public AuthController(AuthService authService, com.phenikaa.security.impossibletravel.service.EmailOtpService emailOtpService) {
        this.authService = authService;
        this.emailOtpService = emailOtpService;
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
        return authResponse(response);
    }

    public record OtpRequest(String challengeId, String code) {}

    @PostMapping("/otp/verify")
    public ResponseEntity<LoginResponseDto> verify(@RequestBody OtpRequest request) {
        return authResponse(authService.verifyEmailCode(request.challengeId(), request.code()));
    }

    @PostMapping("/otp/resend")
    public ResponseEntity<LoginResponseDto> resend(@RequestBody OtpRequest request) {
        return authResponse(emailOtpService.resend(request.challengeId()));
    }

    @PostMapping("/otp/cancel")
    public Map<String, Object> cancel(@RequestBody OtpRequest request) {
        emailOtpService.cancel(request.challengeId());
        return Map.of("success", true, "message", "Đã hủy yêu cầu xác minh");
    }

    private ResponseEntity<LoginResponseDto> authResponse(LoginResponseDto response) {
        return ResponseEntity.status(response.getStatus() != null ? response.getStatus() : 400).body(response);
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
