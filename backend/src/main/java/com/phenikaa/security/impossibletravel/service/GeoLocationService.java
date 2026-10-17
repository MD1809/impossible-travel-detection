package com.phenikaa.security.impossibletravel.service;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.phenikaa.security.impossibletravel.dto.GeoLocationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Service
public class GeoLocationService {

    private static final Logger log = LoggerFactory.getLogger(GeoLocationService.class);

    // Mặc định tọa độ fallback cho môi trường dev local (Hà Nội, Việt Nam)
    public static final double DEFAULT_FALLBACK_LAT = 21.0285;
    public static final double DEFAULT_FALLBACK_LON = 105.8542;
    public static final String DEFAULT_FALLBACK_CITY = "Hanoi";
    public static final String DEFAULT_FALLBACK_COUNTRY = "Vietnam";

    private final DatabaseReader databaseReader;

    @Autowired
    public GeoLocationService(DatabaseReader databaseReader) {
        this.databaseReader = databaseReader;
    }

    /**
     * Phân giải địa chỉ IP thành thông tin vị trí địa lý thực tế.
     *
     * @param ipAddress Chuỗi địa chỉ IP (IPv4 hoặc IPv6)
     * @return Đối tượng GeoLocationDto chứa tọa độ và tên địa danh
     */
    public GeoLocationDto resolve(String ipAddress) {
        return resolveIp(ipAddress);
    }

    public GeoLocationDto resolveIp(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            return new GeoLocationDto("unknown", DEFAULT_FALLBACK_CITY, DEFAULT_FALLBACK_COUNTRY, "VN",
                    DEFAULT_FALLBACK_LAT, DEFAULT_FALLBACK_LON, false);
        }

        String cleanedIp = ipAddress.trim();

        // 1. Kiểm tra dải IP Loopback / Private nội bộ (127.0.0.1, localhost, 192.168.x.x, 10.x.x.x)
        if (isPrivateOrLoopbackIp(cleanedIp)) {
            log.debug("IP [{}] là địa chỉ nội bộ/loopback, sử dụng tọa độ fallback Hà Nội", cleanedIp);
            return new GeoLocationDto(cleanedIp, DEFAULT_FALLBACK_CITY + " (Local)", DEFAULT_FALLBACK_COUNTRY, "VN",
                    DEFAULT_FALLBACK_LAT, DEFAULT_FALLBACK_LON, true);
        }

        // 2. Tra cứu IP công khai qua cơ sở dữ liệu MaxMind GeoLite2 City
        try {
            InetAddress ip = InetAddress.getByName(cleanedIp);
            CityResponse response = databaseReader.city(ip);

            String city = response.getCity().getName() != null ? response.getCity().getName() : "Unknown City";
            String country = response.getCountry().getName() != null ? response.getCountry().getName() : "Unknown Country";
            String countryCode = response.getCountry().getIsoCode() != null ? response.getCountry().getIsoCode() : "";
            Double latitude = response.getLocation().getLatitude() != null ? response.getLocation().getLatitude() : 0.0;
            Double longitude = response.getLocation().getLongitude() != null ? response.getLocation().getLongitude() : 0.0;

            log.info("Phân giải IP [{}] thành công: {}, {} [lat={}, lon={}]",
                    cleanedIp, city, country, latitude, longitude);

            return new GeoLocationDto(cleanedIp, city, country, countryCode, latitude, longitude, true);

        } catch (AddressNotFoundException e) {
            log.warn("Không tìm thấy thông tin địa lý cho IP [{}]: {}", cleanedIp, e.getMessage());
            return new GeoLocationDto(cleanedIp, "Unknown City", "Unknown Country", "", 0.0, 0.0, false);
        } catch (UnknownHostException e) {
            log.error("Địa chỉ IP không đúng định dạng [{}]: {}", cleanedIp, e.getMessage());
            return new GeoLocationDto(cleanedIp, "Invalid IP", "Unknown", "", 0.0, 0.0, false);
        } catch (IOException | GeoIp2Exception e) {
            log.error("Lỗi khi tra cứu cơ sở dữ liệu MaxMind cho IP [{}]: {}", cleanedIp, e.getMessage());
            return new GeoLocationDto(cleanedIp, "Lookup Error", "Unknown", "", 0.0, 0.0, false);
        }
    }

    /**
     * Kiểm tra xem IP có thuộc mạng riêng/loopback hay không.
     */
    private boolean isPrivateOrLoopbackIp(String ip) {
        return "127.0.0.1".equals(ip) ||
                "localhost".equalsIgnoreCase(ip) ||
                "0:0:0:0:0:0:0:1".equals(ip) ||
                "::1".equals(ip) ||
                ip.startsWith("192.168.") ||
                ip.startsWith("10.") ||
                ip.startsWith("172.16.") ||
                ip.startsWith("172.31.");
    }
}
