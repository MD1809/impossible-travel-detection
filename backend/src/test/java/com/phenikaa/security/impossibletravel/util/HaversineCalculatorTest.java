package com.phenikaa.security.impossibletravel.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử thuật toán Haversine và tính toán tốc độ di chuyển")
class HaversineCalculatorTest {

    // Tọa độ chuẩn các thành phố theo tài liệu 06_IMPLEMENTATION_PLAN.md
    private static final double HANOI_LAT = 21.0285;
    private static final double HANOI_LON = 105.8542;

    private static final double HCMC_LAT = 10.8231;
    private static final double HCMC_LON = 106.6297;

    private static final double NEW_YORK_LAT = 40.7128;
    private static final double NEW_YORK_LON = -74.0060;

    private static final double SINGAPORE_LAT = 1.3521;
    private static final double SINGAPORE_LON = 103.8198;

    private static final double TOKYO_LAT = 35.6762;
    private static final double TOKYO_LON = 139.6503;

    @Test
    @DisplayName("Khoảng cách Hà Nội đến TP.HCM phải xấp xỉ 1.130 - 1.150 km")
    void testHanoiToHcmcDistance() {
        double distance = HaversineCalculator.calculateDistance(HANOI_LAT, HANOI_LON, HCMC_LAT, HCMC_LON);
        
        // Khoảng cách thực tế bề mặt cầu Haversine giữa 2 tọa độ trung tâm khoảng 1.140 km
        assertTrue(distance >= 1130.0 && distance <= 1160.0,
                "Khoảng cách Hà Nội -> TP.HCM không khớp mốc chuẩn: " + distance + " km");
    }

    @Test
    @DisplayName("Khoảng cách Hà Nội đến New York phải xấp xỉ 13.125 km")
    void testHanoiToNewYorkDistance() {
        double distance = HaversineCalculator.calculateDistance(HANOI_LAT, HANOI_LON, NEW_YORK_LAT, NEW_YORK_LON);
        
        assertTrue(distance >= 13000.0 && distance <= 13300.0,
                "Khoảng cách Hà Nội -> New York không khớp mốc chuẩn: " + distance + " km");
    }

    @Test
    @DisplayName("Khoảng cách giữa hai điểm trùng nhau phải bằng 0.0 km")
    void testSameCoordinateDistance() {
        double distance = HaversineCalculator.calculateDistance(HANOI_LAT, HANOI_LON, HANOI_LAT, HANOI_LON);
        assertEquals(0.0, distance, 0.001, "Hai điểm trùng nhau khoảng cách phải bằng 0 km");
    }

    @Test
    @DisplayName("Tính toán tốc độ di chuyển bình thường: Hà Nội -> TP.HCM sau 2 giờ bay (~570 km/h < 900 km/h)")
    void testNormalFlightSpeed() {
        double distance = HaversineCalculator.calculateDistance(HANOI_LAT, HANOI_LON, HCMC_LAT, HCMC_LON);
        long twoHoursInSeconds = 2 * 3600;

        double speed = HaversineCalculator.calculateSpeed(distance, twoHoursInSeconds);

        assertTrue(speed > 550.0 && speed < 600.0, "Tốc độ bay 2 giờ Hà Nội - TP.HCM: " + speed + " km/h");
        assertTrue(speed < 900.0, "Tốc độ phải nằm trong ngưỡng an toàn hợp lệ");
    }

    @Test
    @DisplayName("Tính toán tốc độ bất khả thi: Hà Nội -> TP.HCM chỉ sau 5 phút (~13.680 km/h > 900 km/h)")
    void testImpossibleTravelHanoiToHcmcInFiveMinutes() {
        double distance = HaversineCalculator.calculateDistance(HANOI_LAT, HANOI_LON, HCMC_LAT, HCMC_LON);
        long fiveMinutesInSeconds = 5 * 60;

        double speed = HaversineCalculator.calculateSpeed(distance, fiveMinutesInSeconds);

        assertTrue(speed > 13000.0, "Tốc độ phải cực lớn báo động bất khả thi: " + speed + " km/h");
        assertTrue(speed > 900.0, "Tốc độ phải vượt xa ngưỡng máy bay tối đa 900 km/h");
    }

    @Test
    @DisplayName("Đăng nhập đồng thời tức thì tại 2 nơi khác nhau (5 giây) -> Vận tốc hàng triệu km/h")
    void testConcurrentInstantaneousLoginSpeed() {
        double distance = HaversineCalculator.calculateDistance(HANOI_LAT, HANOI_LON, NEW_YORK_LAT, NEW_YORK_LON);
        long fiveSeconds = 5;

        double speed = HaversineCalculator.calculateSpeed(distance, fiveSeconds);

        assertTrue(speed > 9000000.0, "Tốc độ phải ở mức hàng triệu km/h (bất khả thi tuyệt đối): " + speed);
    }

    @Test
    @DisplayName("Trường hợp khoảng cách bằng 0 thì tốc độ phải luôn bằng 0")
    void testZeroDistanceZeroSpeed() {
        double speed = HaversineCalculator.calculateSpeed(0.0, 100);
        assertEquals(0.0, speed, 0.001);
    }

    @Test
    @DisplayName("Thời gian bằng 0 hoặc âm với khoảng cách > 0 phải trả về vô cực POSITIVE_INFINITY")
    void testNonZeroDistanceWithZeroTimeDelta() {
        double speedZeroTime = HaversineCalculator.calculateSpeed(100.0, 0);
        assertEquals(Double.POSITIVE_INFINITY, speedZeroTime);

        double speedNegativeTime = HaversineCalculator.calculateSpeed(100.0, -10);
        assertEquals(Double.POSITIVE_INFINITY, speedNegativeTime);
    }

    @Test
    @DisplayName("Kiểm tra ngoại lệ khi truyền tọa độ không hợp lệ")
    void testInvalidCoordinatesThrowException() {
        // Vĩ độ vượt quá +90 hoặc -90
        assertThrows(IllegalArgumentException.class, () ->
                HaversineCalculator.calculateDistance(95.0, 105.0, HCMC_LAT, HCMC_LON));
        assertThrows(IllegalArgumentException.class, () ->
                HaversineCalculator.calculateDistance(-91.0, 105.0, HCMC_LAT, HCMC_LON));

        // Kinh độ vượt quá +180 hoặc -180
        assertThrows(IllegalArgumentException.class, () ->
                HaversineCalculator.calculateDistance(HANOI_LAT, 185.0, HCMC_LAT, HCMC_LON));
        assertThrows(IllegalArgumentException.class, () ->
                HaversineCalculator.calculateDistance(HANOI_LAT, -190.0, HCMC_LAT, HCMC_LON));
    }
}
