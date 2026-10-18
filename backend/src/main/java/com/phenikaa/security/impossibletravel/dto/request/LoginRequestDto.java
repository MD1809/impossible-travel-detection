package com.phenikaa.security.impossibletravel.dto.request;

public class LoginRequestDto {

    private String username;
    private String password;
    private String ipAddress;
    private String loginTime;

    public LoginRequestDto() {
    }

    public LoginRequestDto(String username, String password, String ipAddress) {
        this.username = username;
        this.password = password;
        this.ipAddress = ipAddress;
    }

    public LoginRequestDto(String username, String password, String ipAddress, String loginTime) {
        this.username = username;
        this.password = password;
        this.ipAddress = ipAddress;
        this.loginTime = loginTime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(String loginTime) {
        this.loginTime = loginTime;
    }
}
