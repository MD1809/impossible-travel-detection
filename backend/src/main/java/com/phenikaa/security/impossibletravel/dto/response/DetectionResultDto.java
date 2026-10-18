package com.phenikaa.security.impossibletravel.dto.response;

import com.phenikaa.security.impossibletravel.entity.EventType;
import com.phenikaa.security.impossibletravel.entity.Severity;

import java.time.LocalDateTime;

/**
 * DTO chứa kết quả đánh giá rủi ro Impossible Travel của Engine
 */
public class DetectionResultDto {

    private boolean blocked;
    private EventType eventType;
    private Severity severity;
    private double distanceKm;
    private long timeDiffSeconds;
    private double timeDiffMinutes;
    private double calculatedSpeedKmh;
    private double thresholdSpeedKmh;
    private String reason;

    // Thông tin lần đăng nhập hiện tại
    private String currentIp;
    private String currentCity;
    private String currentCountry;
    private Double currentLatitude;
    private Double currentLongitude;
    private LocalDateTime currentTime;

    // Thông tin mốc đối chiếu (Active session hoặc Lần đăng nhập trước)
    private String refIp;
    private String refCity;
    private String refCountry;
    private Double refLatitude;
    private Double refLongitude;
    private LocalDateTime refTime;

    public DetectionResultDto() {
    }

    public static DetectionResultDto allowed(double threshold) {
        DetectionResultDto res = new DetectionResultDto();
        res.setBlocked(false);
        res.setSeverity(Severity.LOW);
        res.setThresholdSpeedKmh(threshold);
        res.setReason("Hợp lệ: Vận tốc và vị trí nằm trong giới hạn an toàn");
        return res;
    }

    public static DetectionResultDto blocked(
            EventType eventType,
            Severity severity,
            double distanceKm,
            long timeDiffSeconds,
            double speedKmh,
            double threshold,
            String reason) {
        DetectionResultDto res = new DetectionResultDto();
        res.setBlocked(true);
        res.setEventType(eventType);
        res.setSeverity(severity);
        res.setDistanceKm(distanceKm);
        res.setTimeDiffSeconds(timeDiffSeconds);
        res.setTimeDiffMinutes(Math.round((timeDiffSeconds / 60.0) * 1000.0) / 1000.0);
        res.setCalculatedSpeedKmh(speedKmh);
        res.setThresholdSpeedKmh(threshold);
        res.setReason(reason);
        return res;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public long getTimeDiffSeconds() {
        return timeDiffSeconds;
    }

    public void setTimeDiffSeconds(long timeDiffSeconds) {
        this.timeDiffSeconds = timeDiffSeconds;
    }

    public double getTimeDiffMinutes() {
        return timeDiffMinutes;
    }

    public void setTimeDiffMinutes(double timeDiffMinutes) {
        this.timeDiffMinutes = timeDiffMinutes;
    }

    public double getCalculatedSpeedKmh() {
        return calculatedSpeedKmh;
    }

    public void setCalculatedSpeedKmh(double calculatedSpeedKmh) {
        this.calculatedSpeedKmh = calculatedSpeedKmh;
    }

    public double getThresholdSpeedKmh() {
        return thresholdSpeedKmh;
    }

    public void setThresholdSpeedKmh(double thresholdSpeedKmh) {
        this.thresholdSpeedKmh = thresholdSpeedKmh;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getCurrentIp() {
        return currentIp;
    }

    public void setCurrentIp(String currentIp) {
        this.currentIp = currentIp;
    }

    public String getCurrentCity() {
        return currentCity;
    }

    public void setCurrentCity(String currentCity) {
        this.currentCity = currentCity;
    }

    public String getCurrentCountry() {
        return currentCountry;
    }

    public void setCurrentCountry(String currentCountry) {
        this.currentCountry = currentCountry;
    }

    public Double getCurrentLatitude() {
        return currentLatitude;
    }

    public void setCurrentLatitude(Double currentLatitude) {
        this.currentLatitude = currentLatitude;
    }

    public Double getCurrentLongitude() {
        return currentLongitude;
    }

    public void setCurrentLongitude(Double currentLongitude) {
        this.currentLongitude = currentLongitude;
    }

    public LocalDateTime getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(LocalDateTime currentTime) {
        this.currentTime = currentTime;
    }

    public String getRefIp() {
        return refIp;
    }

    public void setRefIp(String refIp) {
        this.refIp = refIp;
    }

    public String getRefCity() {
        return refCity;
    }

    public void setRefCity(String refCity) {
        this.refCity = refCity;
    }

    public String getRefCountry() {
        return refCountry;
    }

    public void setRefCountry(String refCountry) {
        this.refCountry = refCountry;
    }

    public Double getRefLatitude() {
        return refLatitude;
    }

    public void setRefLatitude(Double refLatitude) {
        this.refLatitude = refLatitude;
    }

    public Double getRefLongitude() {
        return refLongitude;
    }

    public void setRefLongitude(Double refLongitude) {
        this.refLongitude = refLongitude;
    }

    public LocalDateTime getRefTime() {
        return refTime;
    }

    public void setRefTime(LocalDateTime refTime) {
        this.refTime = refTime;
    }
}
