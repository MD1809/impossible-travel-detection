package com.phenikaa.security.impossibletravel.dto.response;

import com.phenikaa.security.impossibletravel.entity.Severity;

import java.time.LocalDateTime;

public class SecurityEventListItemDto {

    private Long id;
    private Long userId;
    private String username;
    private String eventType;
    private String ipAddress;
    private String city;
    private String country;
    private String refCity;
    private String refCountry;
    private Double distanceKm;
    private Double calculatedSpeedKmh;
    private Severity severity;
    private String actionTaken;
    private LocalDateTime createdAt;

    public SecurityEventListItemDto() {
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

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Double getCalculatedSpeedKmh() {
        return calculatedSpeedKmh;
    }

    public void setCalculatedSpeedKmh(Double calculatedSpeedKmh) {
        this.calculatedSpeedKmh = calculatedSpeedKmh;
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
