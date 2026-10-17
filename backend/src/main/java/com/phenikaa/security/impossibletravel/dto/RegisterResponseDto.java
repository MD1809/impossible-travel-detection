package com.phenikaa.security.impossibletravel.dto;

public class RegisterResponseDto {

    private boolean success;
    private String message;
    private Long userId;
    private String username;

    public RegisterResponseDto() {
    }

    public RegisterResponseDto(boolean success, String message, Long userId, String username) {
        this.success = success;
        this.message = message;
        this.userId = userId;
        this.username = username;
    }

    public static RegisterResponseDto success(Long userId, String username) {
        return new RegisterResponseDto(true, "Đăng ký tài khoản thành công", userId, username);
    }

    public static RegisterResponseDto error(String message) {
        return new RegisterResponseDto(false, message, null, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
