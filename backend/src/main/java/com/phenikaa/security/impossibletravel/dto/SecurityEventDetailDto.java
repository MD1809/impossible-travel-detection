package com.phenikaa.security.impossibletravel.dto;

import com.phenikaa.security.impossibletravel.entity.Severity;

import java.time.LocalDateTime;

public class SecurityEventDetailDto {

    private Long id;
    private Long userId;
    private String username;
    private String eventType;
    private Severity severity;
    private String actionTaken;
    private LocalDateTime createdAt;

    private LocationDetail currentAttempt;
    private LocationDetail referenceAttempt;
    private MetricsDetail metrics;

    public SecurityEventDetailDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public String getActionTaken() {
        return actionTaken;
    }

    public void setActionTaken(String actionTaken) {
        this.actionTaken = actionTaken;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocationDetail getCurrentAttempt() {
        return currentAttempt;
    }

    public void setCurrentAttempt(LocationDetail currentAttempt) {
        this.currentAttempt = currentAttempt;
    }

    public LocationDetail getReferenceAttempt() {
        return referenceAttempt;
    }

    public void setReferenceAttempt(LocationDetail referenceAttempt) {
        this.referenceAttempt = referenceAttempt;
    }

    public MetricsDetail getMetrics() {
        return metrics;
    }

    public void setMetrics(MetricsDetail metrics) {
        this.metrics = metrics;
    }

    public static class LocationDetail {
        private String ipAddress;
        private String city;
        private String country;
        private Double latitude;
        private Double longitude;
        private LocalDateTime time;

        public LocationDetail() {
        }

        public LocationDetail(String ipAddress, String city, String country, Double latitude, Double longitude, LocalDateTime time) {
            this.ipAddress = ipAddress;
            this.city = city;
            this.country = country;
            this.latitude = latitude;
            this.longitude = longitude;
            this.time = time;
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

        public LocalDateTime getTime() {
            return time;
        }

        public void setTime(LocalDateTime time) {
            this.time = time;
        }
    }

    public static class MetricsDetail {
        private Double distanceKm;
        private Long timeDiffSeconds;
        private Double timeDiffMinutes;
        private Double calculatedSpeedKmh;
        private Double thresholdSpeedKmh;

        public MetricsDetail() {
        }

        public MetricsDetail(Double distanceKm, Long timeDiffSeconds, Double timeDiffMinutes, Double calculatedSpeedKmh, Double thresholdSpeedKmh) {
            this.distanceKm = distanceKm;
            this.timeDiffSeconds = timeDiffSeconds;
            this.timeDiffMinutes = timeDiffMinutes;
            this.calculatedSpeedKmh = calculatedSpeedKmh;
            this.thresholdSpeedKmh = thresholdSpeedKmh;
        }

        public Double getDistanceKm() {
            return distanceKm;
        }

        public void setDistanceKm(Double distanceKm) {
            this.distanceKm = distanceKm;
        }

        public Long getTimeDiffSeconds() {
            return timeDiffSeconds;
        }

        public void setTimeDiffSeconds(Long timeDiffSeconds) {
            this.timeDiffSeconds = timeDiffSeconds;
        }

        public Double getTimeDiffMinutes() {
            return timeDiffMinutes;
        }

        public void setTimeDiffMinutes(Double timeDiffMinutes) {
            this.timeDiffMinutes = timeDiffMinutes;
        }

        public Double getCalculatedSpeedKmh() {
            return calculatedSpeedKmh;
        }

        public void setCalculatedSpeedKmh(Double calculatedSpeedKmh) {
            this.calculatedSpeedKmh = calculatedSpeedKmh;
        }

        public Double getThresholdSpeedKmh() {
            return thresholdSpeedKmh;
        }

        public void setThresholdSpeedKmh(Double thresholdSpeedKmh) {
            this.thresholdSpeedKmh = thresholdSpeedKmh;
        }
    }
}
