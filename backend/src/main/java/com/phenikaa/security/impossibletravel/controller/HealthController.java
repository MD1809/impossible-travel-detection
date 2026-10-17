package com.phenikaa.security.impossibletravel.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final com.phenikaa.security.impossibletravel.service.GeoLocationService geoLocationService;

    public HealthController(com.phenikaa.security.impossibletravel.service.GeoLocationService geoLocationService) {
        this.geoLocationService = geoLocationService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "impossible-travel-detection");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/test-geoip")
    public ResponseEntity<com.phenikaa.security.impossibletravel.dto.GeoLocationDto> testGeoIp(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "113.160.224.1") String ip) {
        return ResponseEntity.ok(geoLocationService.resolveIp(ip));
    }
}
