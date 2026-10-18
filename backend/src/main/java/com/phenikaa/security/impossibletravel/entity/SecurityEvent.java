package com.phenikaa.security.impossibletravel.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "security_events")
public class SecurityEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String country;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "ref_city", length = 100)
    private String refCity;

    @Column(name = "ref_country", length = 100)
    private String refCountry;

    @Column(name = "ref_latitude")
    private Double refLatitude;

    @Column(name = "ref_longitude")
    private Double refLongitude;

    @Column(name = "ref_ip", length = 45)
    private String refIp;

    @Column(name = "ref_time")
    private LocalDateTime refTime;

    @Column(name = "distance_km")
    private Double distanceKm;

    @Column(name = "time_diff_minutes")
    private Double timeDiffMinutes;

    @Column(name = "calculated_speed_kmh")
    private Double calculatedSpeedKmh;

    @Column(name = "threshold_speed_kmh")
    private Double thresholdSpeedKmh = 900.00;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Severity severity = Severity.HIGH;

    @Column(name = "action_taken", length = 50)
    private String actionTaken = "BLOCKED";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public SecurityEvent() {
    }

    public SecurityEvent(Long id, Long userId, String eventType, String ipAddress, String city, String country, Double latitude, Double longitude, String refCity, String refCountry, Double refLatitude, Double refLongitude, LocalDateTime refTime, Double distanceKm, Double timeDiffMinutes, Double calculatedSpeedKmh, Double thresholdSpeedKmh, Severity severity, String actionTaken, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.eventType = eventType;
        this.ipAddress = ipAddress;
        this.city = city;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
        this.refCity = refCity;
        this.refCountry = refCountry;
        this.refLatitude = refLatitude;
        this.refLongitude = refLongitude;
        this.refTime = refTime;
        this.distanceKm = distanceKm;
        this.timeDiffMinutes = timeDiffMinutes;
        this.calculatedSpeedKmh = calculatedSpeedKmh;
        this.thresholdSpeedKmh = thresholdSpeedKmh != null ? thresholdSpeedKmh : 900.00;
        this.severity = severity != null ? severity : Severity.HIGH;
        this.actionTaken = actionTaken != null ? actionTaken : "BLOCKED";
        this.createdAt = createdAt;
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

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
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

    public String getRefIp() {
        return refIp;
    }

    public void setRefIp(String refIp) {
        this.refIp = refIp;
    }

    public LocalDateTime getRefTime() {
        return refTime;
    }

    public void setRefTime(LocalDateTime refTime) {
        this.refTime = refTime;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
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
}
