# 🛡️ Impossible Travel Detection System

> **Hệ thống Phát hiện Di chuyển Bất khả thi (Impossible Travel Detection)**  
> **Học phần:** Bảo Mật Ứng Dụng và Hệ Thống — Khoa Công nghệ Thông tin, Đại học Phenikaa.

---

## 📋 Mục lục
1. [Giới thiệu tổng quan](#1-giới-thiệu-tổng-quan)
2. [Nguyên lý hoạt động & Thuật toán](#2-nguyên-lý-hoạt-động--thuật-toán)
3. [Kiến trúc hệ thống & Công nghệ](#3-kiến-trúc-hệ-thống--công-nghệ)
4. [Cấu trúc thư mục dự án](#4-cấu-trúc-thư-mục-dự-án)
5. [Yêu cầu môi trường (Prerequisites)](#5-yêu-cầu-môi-trường-prerequisites)
6. [Hướng dẫn cài đặt & Khởi chạy](#6-hướng-dẫn-cài-đặt--khởi-chạy)
7. [Giao diện SOC Dark Admin Dashboard](#7-giao-diện-soc-dark-admin-dashboard)
8. [Kịch bản Kiểm thử & Mô phỏng (Demonstration)](#8-kịch-bản-kiểm-thử--mô-phỏng-demonstration)
9. [Tài liệu API (REST Endpoints)](#9-tài-liệu-api-rest-endpoints)

---

## 1. Giới thiệu tổng quan

**Impossible Travel Detection** là một kỹ thuật bảo mật cốt lõi trong phân hệ **UEBA (User and Entity Behavior Analytics)** và các hệ thống **SIEM/SOC** hiện đại. 

Hệ thống tự động phát hiện, cảnh báo và ngăn chặn hành vi truy cập trái phép bằng cách tính toán khoảng cách địa lý và thời gian chênh lệch giữa hai lần đăng nhập liên tiếp hoặc giữa các phiên hoạt động đồng thời của cùng một tài khoản. Nếu vận tốc di chuyển lý thuyết vượt quá ngưỡng tối đa của phương tiện hàng không thương mại thông thường (mặc định $900\text{ km/h}$), hệ thống sẽ lập tức:
- 🚫 **Chặn đăng nhập (HTTP 403 Forbidden)** và vô hiệu hóa nỗ lực xâm nhập.
- 🚨 **Ghi nhận sự kiện an ninh (Security Event)** với mức độ nghiêm trọng `CRITICAL` hoặc `HIGH`.
- 📊 **Cập nhật thời gian thực lên SOC Dashboard** phục vụ điều tra số (Forensics Investigation).

---

## 2. Nguyên lý hoạt động & Thuật toán

### 2.1. Hai kịch bản phát hiện (Detection Scenarios)

1. **Kịch bản 1: Đăng nhập đồng thời bất thường (`CONCURRENT_IMPOSSIBLE_TRAVEL`)**
   - Tài khoản đang có phiên làm việc hoạt động (`is_active = true`) tại vị trí $A$ (Ví dụ: Hà Nội).
   - Có yêu cầu đăng nhập mới từ vị trí $B$ (Ví dụ: New York) sau một khoảng thời gian cực ngắn $\Delta t < 60\text{s}$ hoặc với khoảng cách quá xa $d > 50\text{ km}$.
   - **Xử lý:** Chặn đăng nhập mới ngay lập tức để bảo vệ tài khoản; giữ nguyên phiên làm việc hợp lệ tại vị trí $A$.
2. **Kịch bản 2: Di chuyển bất khả thi tuần tự (`SEQUENTIAL_IMPOSSIBLE_TRAVEL`)**
   - Tài khoản đăng xuất hoặc hết hạn tại vị trí $A$, sau đó đăng nhập tại vị trí $B$.
   - Tính toán vận tốc dựa trên lần đăng nhập thành công gần nhất: $v = \frac{d}{\Delta t}$.
   - Nếu $v > 900\text{ km/h}$, hệ thống chặn truy cập và gắn cờ `BLOCKED`.

### 2.2. Công thức khoảng cách Haversine (Great-Circle Distance)

Khoảng cách trên bề mặt hình cầu Trái Đất giữa tọa độ $(\phi_1, \lambda_1)$ và $(\phi_2, \lambda_2)$ được tính bằng công thức Haversine với bán kính Trái Đất $R = 6371.0\text{ km}$:

$$\Delta\phi = \phi_2 - \phi_1, \quad \Delta\lambda = \lambda_2 - \lambda_1$$

$$a = \sin^2\left(\frac{\Delta\phi}{2}\right) + \cos(\phi_1) \cdot \cos(\phi_2) \cdot \sin^2\left(\frac{\Delta\lambda}{2}\right)$$

$$c = 2 \cdot \arctan2\left(\sqrt{a}, \sqrt{1 - a}\right)$$

$$d = R \cdot c$$

### 2.3. Tính toán vận tốc & Đánh giá an ninh

$$v = \frac{d}{\Delta t_{\text{giờ}}} = \frac{d}{\Delta t_{\text{giây}} / 3600}$$

- Nếu $\Delta t = 0$ (hoặc $\le 1\text{ giây}$) và $d > 50\text{ km} \rightarrow$ Vận tốc tiếp cận vô cùng ($\infty$) $\rightarrow$ Cảnh báo `CRITICAL`.
- Nếu $v > 900\text{ km/h} \rightarrow$ Vi phạm Impossible Travel $\rightarrow$ Cảnh báo `CRITICAL` / `HIGH`.
- Nếu $v \le 900\text{ km/h} \rightarrow$ Hợp lệ $\rightarrow$ Trạng thái `ALLOWED` (HTTP 200).

---

## 3. Kiến trúc hệ thống & Công nghệ

```
┌─────────────────────────────────────────────────────────────┐
│                   ReactJS SOC Dashboard                     │
│    (Vite, Tailwind Tokens, Lucide Icons, Cyber Dark UI)     │
└──────────────────────────────┬──────────────────────────────┘
                               │ HTTP / JSON (Axios)
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                 Spring Boot 3 REST Backend                  │
│  ┌────────────────────┬──────────────────┬───────────────┐  │
│  │ GeoLocationService │ ImpossibleTravel │ Auth / SOC    │  │
│  │ (MaxMind GeoLite2) │ Detection Engine │ Controllers   │  │
│  └────────────────────┴──────────────────┴───────────────┘  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │      Spring Data JPA + Hibernate + Flyway Schema      │  │
│  └───────────────────────────────────────────────────────┘  │
└──────────────────────────────┬──────────────────────────────┘
                               │ JDBC (Port 3306)
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                      MySQL 8 Database                       │
│    [users] [user_sessions] [login_history]                  │
│    [security_events] [system_configs]                       │
└──────────────────────────────┘
```

- **Backend:** Java 21 LTS, Spring Boot 3.3.4, Spring Security 6, Spring Data JPA, MaxMind GeoIP2 Java API (`v4.2.0`), Flyway Migration, BCrypt Password Encoder, JUnit 5 & MockMvc.
- **Cơ sở dữ liệu:** MySQL 8.x, MaxMind `GeoLite2-City.mmdb` (offline database gồm hàng triệu dải IP toàn cầu).
- **Frontend:** React 19, Vite 5, Axios, Lucide React icons, giao diện SOC Dark Cyberpunk chuẩn Trung tâm Vận hành An ninh mạng.

---

## 4. Cấu trúc thư mục dự án

```
impossible-travel-detection/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/phenikaa/security/impossibletravel/
│   │   │   │   ├── config/          # Spring Security, CORS & Bean configs
│   │   │   │   ├── controller/      # REST API Controllers (Auth, Dashboard, Events)
│   │   │   │   ├── dto/             # Data Transfer Objects & API specs
│   │   │   │   ├── entity/          # JPA Entities (User, Session, History, Event, Config)
│   │   │   │   ├── repository/      # Spring Data JPA Repositories
│   │   │   │   ├── service/         # Engine logic, GeoLocation & Auth Services
│   │   │   │   └── util/            # HaversineCalculator
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       ├── db/migration/    # V1 Flyway SQL schema & seed data
│   │   │       └── geoip/           # GeoLite2-City.mmdb (50.8 MB)
│   │   └── test/                    # Unit tests & End-to-End Integration tests
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── components/              # Header, StatCards, Table, Modal, LiveSimulation
│   │   ├── services/                # Axios API Client
│   │   ├── App.jsx                  # Main SOC dashboard application
│   │   └── index.css                # Dark SOC theme & animations
│   ├── package.json
│   └── vite.config.js
├── postman/
│   ├── Impossible_Travel_Detection.postman_collection.json
│   └── Impossible_Travel_Environment.postman_environment.json
└── README.md
```

---

## 5. Yêu cầu môi trường (Prerequisites)

Trước khi khởi chạy hệ thống, hãy đảm bảo máy tính đã cài đặt:
1. **Java Development Kit (JDK):** Phiên bản **21 LTS** hoặc mới hơn (`java -version`).
2. **Apache Maven:** Phiên bản **3.9+** (`mvn -v`).
3. **Node.js & npm:** Node.js **18.x** trở lên (`node -v`, `npm -v`).
4. **MySQL Server:** Phiên bản **8.0+** đang chạy trên cổng mặc định `3306`.

---

## 6. Hướng dẫn cài đặt & Khởi chạy

### Bước 1: Chuẩn bị Cơ sở Dữ liệu MySQL
Mở MySQL Command Line Client hoặc MySQL Workbench, tạo cơ sở dữ liệu:
```sql
CREATE DATABASE IF NOT EXISTS impossible_travel_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;
```

Kiểm tra cấu hình thông tin đăng nhập trong file `backend/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/impossible_travel_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=123456
```
*(Nếu mật khẩu MySQL của bạn khác `123456`, vui lòng cập nhật lại dòng trên).*

> **Lưu ý:** Flyway sẽ tự động kích hoạt và chạy migration `V1__initial_schema.sql` để tạo đầy đủ 5 bảng và dữ liệu tài khoản quản trị mẫu `admin / Admin@123`.

---

### Bước 2: Khởi chạy Backend (Spring Boot)

Mở terminal tại thư mục gốc của dự án:
```bash
cd backend
mvn clean install -DskipTests
mvn spring-boot:run
```

Sau khi khởi chạy thành công, Backend sẽ hoạt động tại:
- **Root API:** `http://localhost:8080/api`
- **Health Check:** `http://localhost:8080/api/health`
- **GeoIP Test:** `http://localhost:8080/api/health/test-geoip?ip=113.160.224.1`

---

### Bước 3: Khởi chạy Frontend (ReactJS SOC Dashboard)

Mở một cửa sổ terminal mới:
```bash
cd frontend
npm install
npm run dev
```

Truy cập Dashboard tại: `http://localhost:5173` (hoặc cổng được hiển thị bởi Vite).  
Hệ thống Frontend đã được cấu hình proxy tự động chuyển tiếp request `/api/*` về `http://localhost:8080`.

---

### Bước 4: Chạy toàn bộ Test Suite (Kiểm thử tự động)

Tại thư mục `backend/`, thực thi lệnh:
```bash
mvn test
```
Hệ thống sẽ chạy **29 bài kiểm thử tự động**, bao gồm:
- `HaversineCalculatorTest` (9/9 passed): Kiểm thử độ chính xác khoảng cách và vận tốc.
- `ImpossibleTravelEngineTest` (4/4 passed): Kiểm thử 2 kịch bản vi phạm và trường hợp hợp lệ.
- `AuthServiceTest` (5/5 passed): Kiểm thử đăng ký, đăng nhập và đăng xuất.
- `DashboardServiceTest` (3/3 passed): Kiểm thử tổng hợp số liệu SOC.
- `ImpossibleTravelIntegrationTest` (8/8 passed): Kiểm thử luồng E2E tích hợp qua MockMvc.

---

## 7. Giao diện SOC Dark Admin Dashboard

Dashboard được thiết kế theo phong cách Cyberpunk Dark Theme chuyên biệt cho trung tâm giám sát an ninh mạng (SOC):

- **Radar Status Indicator:** Radar quét chu kỳ 10 giây tự động làm mới dữ liệu từ server.
- **Thẻ KPI thời gian thực:**
  - Tổng số lượt đăng nhập (Total Logins).
  - Đăng nhập hợp lệ (Allowed Access).
  - Đăng nhập bị chặn (Blocked Threats).
  - Phiên đang hoạt động (Active Sessions).
  - Tổng số cảnh báo an ninh (Security Alerts).
- **Bảng Nhật ký Sự kiện An ninh (Security Incidents Table):**
  - Cảnh báo Severity: `CRITICAL` (Đỏ neon), `HIGH` (Cam neon), `MEDIUM` (Vàng neon).
  - Hiển thị lộ trình điều tra: `Hà Nội (VN) ➔ New York (US)`.
  - Bộ lọc tìm kiếm nhanh theo username/IP và lọc mức độ nghiêm trọng.
- **Cửa sổ Khám nghiệm Chi tiết (Forensic Investigation Modal):**
  - So sánh chi tiết lần đăng nhập làm mốc vs lần bị chặn.
  - Hộp thông số thuật toán: Khoảng cách ($km$), Thời gian chênh lệch ($\Delta t$), Vận tốc tính toán ($km/h$) và Ngưỡng tối đa.
  - Tỉ lệ vượt ngưỡng (Speed Ratio, ví dụ: $15.2\times$ vượt vận tốc máy bay thương mại).
  - Nút sao chép báo cáo điều tra JSON một chạm.
- **Bảng điều khiển Giả lập Tấn công (Live Attack Simulation):**
  - Tích hợp sẵn 4 nút kịch bản kiểm thử nhanh không cần phần mềm bên thứ ba.

---

## 8. Kịch bản Kiểm thử & Mô phỏng (Demonstration)

Hệ thống có thể được kiểm thử thông qua **Giao diện Giả lập trên Dashboard** hoặc thông qua **Postman Collection** (`postman/Impossible_Travel_Detection.postman_collection.json`).

### Bảng dữ liệu địa chỉ IP & Tọa độ MaxMind tham chiếu:

| Vị trí | Quốc gia | Địa chỉ IP mẫu | Vĩ độ (Lat) | Kinh độ (Lon) |
| :--- | :--- | :--- | :--- | :--- |
| **Hà Nội** | Vietnam | `118.70.1.1` | 21.0285 | 105.8542 |
| **Đà Nẵng** | Vietnam | `113.160.224.1` | 16.0685 | 108.2215 |
| **TP. Hồ Chí Minh**| Vietnam | `171.244.10.1` | 10.8231 | 106.6297 |
| **Singapore** | Singapore | `165.225.112.0` | 1.3521 | 103.8198 |
| **Tokyo** | Japan | `133.242.0.1` | 35.6762 | 139.6503 |
| **New York** | United States | `161.185.160.93` | 40.7128 | -74.0060 |

---

### Kịch bản 1: Đăng nhập đồng thời bất thường (Concurrent Anomaly)
1. **Thao tác:** 
   - Đăng nhập tài khoản `student01` với IP `113.160.224.1` (Hà Nội).
   - Ngay lập tức (sau 3 giây), gửi request đăng nhập cùng tài khoản với IP `161.185.160.93` (New York).
2. **Kết quả:**
   - Request 1: Trả về HTTP `200 OK`, cấp `sessionToken`, tạo phiên hoạt động tại Hà Nội.
   - Request 2: Trả về HTTP `403 Forbidden`, thông báo: `Cảnh báo an ninh: Phát hiện di chuyển bất khả thi (Impossible Travel Detected)`.
   - Vận tốc tính toán: $\approx 16.000.000\text{ km/h} \gg 900\text{ km/h}$.
   - Sự kiện vi phạm được ghi vào cơ sở dữ liệu với mã `CONCURRENT_IMPOSSIBLE_TRAVEL`.

---

### Kịch bản 2: Di chuyển bất khả thi tuần tự (Sequential Travel)
1. **Thao tác:**
   - Đăng nhập tài khoản `student01` tại TP. Hồ Chí Minh (`171.244.10.1`).
   - Đăng xuất phiên làm việc (`POST /api/auth/logout`).
   - 5 phút sau, đăng nhập tại Tokyo (`133.242.0.1`).
2. **Kết quả:**
   - Khoảng cách giữa TP.HCM và Tokyo: $\approx 4.330\text{ km}$.
   - Vận tốc di chuyển: $\approx 51.960\text{ km/h} > 900\text{ km/h}$.
   - Backend chặn đăng nhập với HTTP `403 Forbidden`, phân loại `SEQUENTIAL_IMPOSSIBLE_TRAVEL`.

---

### Kịch bản 3: Di chuyển hợp lệ bình thường (Normal Sequential Travel)
1. **Thao tác:**
   - Đăng nhập tại Hà Nội (`113.160.224.1`) lúc 08:00.
   - 3 tiếng sau (hoặc mô phỏng với mốc thời gian cách nhau 180 phút), đăng nhập tại TP.HCM (`171.244.10.1`).
2. **Kết quả:**
   - Khoảng cách: $\approx 1.130\text{ km}$.
   - Vận tốc trung bình: $\approx 376\text{ km/h} < 900\text{ km/h}$ (khả thi với chuyến bay hàng không nội địa thông thường).
   - Backend trả về HTTP `200 OK`, cho phép đăng nhập thành công.

---

## 9. Tài liệu API (REST Endpoints)

### 9.1. Phân hệ Xác thực (Authentication)

| Phương thức | Endpoint | Mô tả |
| :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Đăng ký tài khoản người dùng mới |
| `POST` | `/api/auth/login` | Đăng nhập tài khoản & kiểm tra Impossible Travel |
| `POST` | `/api/auth/logout` | Đăng xuất và hủy kích hoạt phiên làm việc |

#### Body Request Đăng nhập Mẫu (`POST /api/auth/login`):
```json
{
  "username": "student01",
  "password": "Password@123",
  "ipAddress": "161.185.160.93"
}
```

#### Phản hồi khi bị chặn (`403 Forbidden`):
```json
{
  "success": false,
  "message": "Cảnh báo an ninh: Phát hiện di chuyển bất khả thi (Impossible Travel Detected)",
  "detectionDetails": {
    "ruleTriggered": "CONCURRENT_IMPOSSIBLE_TRAVEL",
    "currentLogin": {
      "city": "Nyack",
      "country": "United States",
      "coordinates": [41.0911, -73.9199],
      "ipAddress": "161.185.160.93",
      "time": "2026-10-17T18:56:24.015"
    },
    "referenceLogin": {
      "city": "Da Nang",
      "country": "Vietnam",
      "coordinates": [16.0685, 108.2215],
      "ipAddress": "113.160.224.1",
      "time": "2026-10-17T18:56:23.901"
    },
    "distanceKm": 13655.39,
    "timeDifferenceMinutes": 0.02,
    "calculatedSpeedKmh": 49159404.0,
    "thresholdSpeedKmh": 900.0,
    "action": "BLOCKED"
  }
}
```

### 9.2. Phân hệ SOC & Quản trị (Dashboard & Security)

| Phương thức | Endpoint | Tham số | Mô tả |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/dashboard/summary` | Không | Lấy thống kê KPI tổng quan cho Dashboard |
| `GET` | `/api/security-events` | `page`, `size`, `severity` | Lấy danh sách sự kiện vi phạm an ninh (phân trang) |
| `GET` | `/api/security-events/{id}` | `id` | Lấy chi tiết khám nghiệm sự kiện vi phạm an ninh |
| `GET` | `/api/sessions/active` | Không | Danh sách các phiên làm việc đang hoạt động |
| `GET` | `/api/login-history` | `userId`, `page`, `size` | Lịch sử đăng nhập toàn hệ thống |

---

## 👨‍💻 Thông tin Tác giả & Đề tài
- **Đề tài:** Xây dựng Cơ chế Phát hiện Di chuyển Bất khả thi (Impossible Travel Detection) Bảo vệ Tài khoản Người dùng.
- **Đơn vị:** Khoa Công nghệ Thông tin — Đại học Phenikaa.
- **Giấy phép:** MIT License.
