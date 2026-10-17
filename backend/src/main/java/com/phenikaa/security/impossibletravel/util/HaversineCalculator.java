package com.phenikaa.security.impossibletravel.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility toán học tính khoảng cách bề mặt cầu giữa hai tọa độ địa lý
 * theo công thức Haversine (Great-Circle Distance).
 */
public final class HaversineCalculator {

    /**
     * Bán kính trung bình Trái Đất theo chuẩn quốc tế (WGS-84 / IUGG): 6371.0 km
     */
    public static final double EARTH_RADIUS_KM = 6371.0;

    private HaversineCalculator() {
        // Utility class, không khởi tạo trực tiếp
    }

    /**
     * Kiểm tra tính hợp lệ của cặp tọa độ Vĩ độ và Kinh độ
     *
     * @param latitude  Vĩ độ (-90.0 đến +90.0)
     * @param longitude Kinh độ (-180.0 đến +180.0)
     * @return true nếu tọa độ hợp lệ
     */
    public static boolean isValidCoordinate(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return false;
        }
        if (Double.isNaN(latitude) || Double.isInfinite(latitude)
                || Double.isNaN(longitude) || Double.isInfinite(longitude)) {
            return false;
        }
        return latitude >= -90.0 && latitude <= 90.0 && longitude >= -180.0 && longitude <= 180.0;
    }

    /**
     * Tính khoảng cách địa lý (Great-Circle Distance) giữa hai tọa độ địa lý (đơn vị: km)
     *
     * @param lat1 Vĩ độ điểm thứ nhất
     * @param lon1 Kinh độ điểm thứ nhất
     * @param lat2 Vĩ độ điểm thứ hai
     * @param lon2 Kinh độ điểm thứ hai
     * @return Khoảng cách theo km
     * @throws IllegalArgumentException nếu tọa độ nằm ngoài phạm vi chuẩn
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        if (!isValidCoordinate(lat1, lon1)) {
            throw new IllegalArgumentException(String.format("Tọa độ điểm 1 không hợp lệ: lat=%.4f, lon=%.4f", lat1, lon1));
        }
        if (!isValidCoordinate(lat2, lon2)) {
            throw new IllegalArgumentException(String.format("Tọa độ điểm 2 không hợp lệ: lat=%.4f, lon=%.4f", lat2, lon2));
        }

        // Tọa độ trùng nhau trả về 0 km ngay lập tức
        if (Double.compare(lat1, lat2) == 0 && Double.compare(lon1, lon2) == 0) {
            return 0.0;
        }

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);

        // a = sin²(Δlat/2) + cos(lat1) * cos(lat2) * sin²(Δlon/2)
        double a = Math.sin(dLat / 2.0) * Math.sin(dLat / 2.0)
                + Math.cos(radLat1) * Math.cos(radLat2) * Math.sin(dLon / 2.0) * Math.sin(dLon / 2.0);

        // Đảm bảo a nằm trong khoảng [0, 1] phòng ngừa sai số dấu phẩy động
        a = Math.min(1.0, Math.max(0.0, a));

        // c = 2 * atan2(√a, √(1-a))
        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));

        return round(EARTH_RADIUS_KM * c, 2);
    }

    /**
     * Tính tốc độ di chuyển suy ra (đơn vị: km/h)
     *
     * @param distanceKm       Khoảng cách giữa 2 điểm (km)
     * @param timeDiffSeconds Khoảng thời gian giữa 2 lần đăng nhập (giây)
     * @return Tốc độ di chuyển km/h. Trả về Double.POSITIVE_INFINITY nếu khoảng cách > 0 và timeDiff <= 0
     */
    public static double calculateSpeed(double distanceKm, long timeDiffSeconds) {
        if (distanceKm <= 0.0) {
            return 0.0;
        }
        if (timeDiffSeconds <= 0) {
            return Double.POSITIVE_INFINITY;
        }

        double hours = timeDiffSeconds / 3600.0;
        double speed = distanceKm / hours;

        return round(speed, 2);
    }

    /**
     * Làm tròn số thực với số chữ số sau dấu phẩy mong muốn
     */
    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException("Số chữ số làm tròn không thể âm");
        }
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return value;
        }
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
