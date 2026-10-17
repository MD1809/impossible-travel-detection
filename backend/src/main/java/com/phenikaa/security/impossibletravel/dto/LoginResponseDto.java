package com.phenikaa.security.impossibletravel.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponseDto {

    private boolean success;
    private Integer status;
    private String error;
    private String message;
    private String sessionToken;
    private UserInfoDto userInfo;
    private LocationDto location;
    private Map<String, Object> detectionDetails;

    public LoginResponseDto() {
    }

    public static LoginResponseDto success(String sessionToken, UserInfoDto userInfo, LocationDto location) {
        LoginResponseDto res = new LoginResponseDto();
        res.setSuccess(true);
        res.setStatus(200);
        res.setMessage("Đăng nhập thành công");
        res.setSessionToken(sessionToken);
        res.setUserInfo(userInfo);
        res.setLocation(location);
        return res;
    }

    public static LoginResponseDto unauthorized(String message) {
        LoginResponseDto res = new LoginResponseDto();
        res.setSuccess(false);
        res.setStatus(401);
        res.setError("Unauthorized");
        res.setMessage(message != null ? message : "Tên đăng nhập hoặc mật khẩu không chính xác");
        return res;
    }

    public static LoginResponseDto forbidden(String message, Map<String, Object> detectionDetails) {
        LoginResponseDto res = new LoginResponseDto();
        res.setSuccess(false);
        res.setStatus(403);
        res.setError("Forbidden");
        res.setMessage(message);
        res.setDetectionDetails(detectionDetails);
        return res;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public UserInfoDto getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfoDto userInfo) {
        this.userInfo = userInfo;
    }

    public LocationDto getLocation() {
        return location;
    }

    public void setLocation(LocationDto location) {
        this.location = location;
    }

    public Map<String, Object> getDetectionDetails() {
        return detectionDetails;
    }

    public void setDetectionDetails(Map<String, Object> detectionDetails) {
        this.detectionDetails = detectionDetails;
    }

    public static class UserInfoDto {
        private Long userId;
        private String username;
        private String role;

        public UserInfoDto() {
        }

        public UserInfoDto(Long userId, String username, String role) {
            this.userId = userId;
            this.username = username;
            this.role = role;
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

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

    public static class LocationDto {
        private String ipAddress;
        private String city;
        private String country;
        private Double latitude;
        private Double longitude;

        public LocationDto() {
        }

        public LocationDto(String ipAddress, String city, String country, Double latitude, Double longitude) {
            this.ipAddress = ipAddress;
            this.city = city;
            this.country = country;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }
    }
}
