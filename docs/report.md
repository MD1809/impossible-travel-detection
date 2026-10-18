# ĐỀ CƯƠNG VÀ CẤU TRÚC CHI TIẾT BÁO CÁO ĐỒ ÁN MÔN HỌC

* **Tên đề tài**: Nghiên cứu và Xây dựng Hệ thống Phát hiện Đăng nhập Bất thường theo Khoảng cách Địa lý (Impossible Travel Detection System)
* **Học phần**: Bảo mật Ứng dụng và Hệ thống
* **Khoa**: Công nghệ Thông tin - Trường Đại học Phenikaa
* **Giảng viên hướng dẫn**: [Họ và tên Giảng viên]
* **Sinh viên thực hiện**: [Họ tên SV 1 - MSSV] & [Họ tên SV 2 - MSSV]
* **Năm học / Học kỳ**: 2025 - 2026

---

## BẢNG PHÂN CÔNG NHIỆM VỤ THÀNH VIÊN TRONG ĐỒ ÁN

> [!IMPORTANT]
> Cả 2 thành viên đều trực tiếp tham gia nghiên cứu và triển khai các kiến thức bảo mật cốt lõi của môn học, được phân chia theo 2 trụ cột an ninh: **Quản lý Định danh & Phiên (IAM)** và **Động cơ Phát hiện Rủi ro & Giám sát SOC (Detection & Telemetry)**.

| Thành viên | Trách nhiệm chuyên môn An ninh phụ trách | Module / Mã nguồn đóng góp | Tỷ lệ đóng góp |
| :--- | :--- | :--- | :---: |
| **Thành viên 1**<br>*(Họ tên - MSSV)* | • **Cơ chế băm mật khẩu**: Nghiên cứu và áp dụng chuẩn BCrypt Hashing kèm Salt ngẫu nhiên chống tấn công Rainbow Table.<br>• **Bảo mật phiên làm việc**: Quản lý vòng đời `UserSession`, cơ chế vô hiệu hóa phiên tức thời (**Session Invalidation**) khi phát hiện bất thường.<br>• **Phân tích mô hình tấn công**: Chiếm quyền tài khoản (**Account Takeover - ATO**), rò rỉ thông tin xác thực (Credential Stuffing).<br>• **Kiểm thử an ninh**: Xây dựng kịch bản kiểm thử phòng thủ (Authentication Security Testing) & Integration Tests. | • `AuthService.java`<br>• `UserSessionRepository.java`<br>• `LoginHistoryRepository.java`<br>• Thiết kế bảng CSDL xác thực (`users`, `user_sessions`, `login_history`)<br>• `AuthServiceTest.java` | **50%** |
| **Thành viên 2**<br>*(Họ tên - MSSV)* | • **Địa lý học an ninh (GeoIP Intelligence)**: Nghiên cứu cấu trúc CSDL MaxMind GeoLite2 và cơ chế phân giải IP ra tọa độ (Lat, Lon).<br>• **Thuật toán đánh giá rủi ro**: Hiện thực hóa công thức mặt cầu **Haversine** và giải thuật tính vận tốc không gian - thời gian ($v = d / \Delta t$).<br>• **Quy tắc phát hiện**: Xây dựng 2 luật an ninh cốt lõi: Đăng nhập đồng thời bất thường (**Concurrent Anomaly**) và Di chuyển nối tiếp bất khả thi (**Sequential Travel**).<br>• **Ghi nhật ký & Giám sát SOC**: Thiết kế lược đồ bảng `SecurityEvent`, phân loại mức độ nghiêm trọng (Severity Scoring) phục vụ điều tra số (Digital Forensics). | • `ImpossibleTravelEngine.java`<br>• `GeoLocationService.java`<br>• `HaversineCalculator.java`<br>• `SecurityEventRepository.java`<br>• Bảng CSDL an ninh (`security_events`, `system_configs`)<br>• `ImpossibleTravelEngineTest.java` | **50%** |
| **Phối hợp chung** | • **Giao diện SOC Dashboard**: Cả 2 cùng phối hợp thiết kế giao diện trực quan hóa dữ liệu giám sát an ninh (bảng sự kiện an ninh, nhật ký truy cập, thẻ chỉ số KPI).<br>• **Báo cáo & Thuyết trình**: Cùng viết Báo cáo tổng kết đồ án, thiết kế Slide trình chiếu và thực hiện Live Demo. | • `frontend/src/App.jsx`<br>• `EventDetailModal.jsx`<br>• `StatCards.jsx` | |

---

## MỤC LỤC TỔNG THỂ BÁO CÁO

* **LỜI CẢM ƠN**
* **LỜI CAM ĐOAN**
* **DANH MỤC TỪ VIẾT TẮT**
* **DANH MỤC HÌNH ẢNH**
* **DANH MỤC BẢNG BIỂU**
* **BẢNG PHÂN CÔNG CÔNG VIỆC CỦA THÀNH VIÊN**
* **CHƯƠNG 1: TỔNG QUAN VÀ ĐẶT VẤN ĐỀ**
  * 1.1. Bối cảnh an ninh thông tin và các hình thức tấn công chiếm quyền tài khoản
  * 1.2. Khái niệm và nguyên lý kỹ thuật "Impossible Travel"
  * 1.3. Mục tiêu, phạm vi và ý nghĩa thực tiễn của đề tài
  * 1.4. Bố cục của báo cáo
* **CHƯƠNG 2: CƠ SỞ LÝ THUYẾT VÀ CÔNG NGHỆ ÁP DỤNG**
  * 2.1. Kỹ thuật định vị địa lý mạng IP (GeoIP Resolution)
  * 2.2. Toán học mặt cầu và Công thức khoảng cách Haversine (Great-Circle Distance)
  * 2.3. Mô hình tính toán vận tốc di chuyển và cơ sở chọn ngưỡng an toàn (900 km/h)
  * 2.4. Nguyên lý xác thực an toàn và quản lý phiên làm việc (Session Security)
  * 2.5. Công nghệ và môi trường phát triển hệ thống
* **CHƯƠNG 3: PHÂN TÍCH VÀ THIẾT KẾ HỆ THỐNG**
  * 3.1. Phân tích các mối đe dọa (Threat Modeling)
  * 3.2. Kiến trúc tổng thể hệ thống (System Architecture)
  * 3.3. Thiết kế chi tiết các luồng xử lý an ninh (Security Workflows)
    * 3.3.1. Kịch bản 1: Phát hiện phiên đăng nhập đồng thời bất thường (Concurrent Session Anomaly)
    * 3.3.2. Kịch bản 2: Phát hiện di chuyển bất khả thi nối tiếp (Sequential Impossible Travel)
    * 3.3.3. Kịch bản 3: Trường hợp người dùng di chuyển hợp lệ bằng đường hàng không (Realistic Travel)
    * 3.3.4. Kịch bản 4: Đăng nhập thông thường cùng vị trí địa lý hoặc sai mật khẩu
  * 3.4. Thiết kế cơ sở dữ liệu quan hệ (Database Schema)
  * 3.5. Thiết kế đặc tả giao tiếp RESTful API
  * 3.6. Thiết kế giao diện giám sát an ninh (SOC Dashboard UI)
* **CHƯƠNG 4: HIỆN THỰC HÓA VÀ CÀI ĐẶT HỆ THỐNG (IMPLEMENTATION)**
  * 4.1. Cài đặt phân hệ Xác thực an toàn và Quản lý phiên (`AuthService`, `UserSession`)
  * 4.2. Cài đặt Động cơ phát hiện di chuyển bất khả thi (`ImpossibleTravelEngine`)
  * 4.3. Cài đặt dịch vụ phân giải địa lý và tính toán khoảng cách (`GeoLocationService`, `HaversineCalculator`)
  * 4.4. Cài đặt hệ thống ghi nhận nhật ký an ninh và giám sát SOC (`DashboardService`, `SecurityEvent`)
  * 4.5. Hiện thực hóa giao diện trực quan hóa SOC Dashboard
* **CHƯƠNG 5: KIỂM THỬ, ĐÁNH GIÁ KẾT QUẢ VÀ THẢO LUẬN**
  * 5.1. Thiết kế môi trường và bộ dữ liệu kiểm thử
  * 5.2. Kết quả kiểm thử các kịch bản an ninh thực tế
  * 5.3. Đánh giá độ chính xác, tỷ lệ cảnh báo sai (False Positive / False Negative)
  * 5.4. Đánh giá hiệu năng và thời gian đáp ứng của hệ thống
* **CHƯƠNG 6: KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN**
  * 6.1. Những kết quả đã đạt được
  * 6.2. Hạn chế hiện tại của đề tài
  * 6.3. Hướng nghiên cứu và phát triển tiếp theo
* **TÀI LIỆU THAM KHẢO**
* **PHỤ LỤC: HƯỚNG DẪN CÀI ĐẶT VÀ MÃ NGUỒN CỐT LÕI**

---

## NỘI DUNG CHI TIẾT CẦN VIẾT CHO TỪNG CHƯƠNG

### CHƯƠNG 1: TỔNG QUAN VÀ ĐẶT VẤN ĐỀ

* **1.1. Bối cảnh an ninh thông tin**:
  * Sự gia tăng mạnh mẽ của các vụ lộ lọt dữ liệu (Data Breaches) và kỹ thuật tấn công vét cạn / rải thông tin xác thực (**Credential Stuffing**).
  * Hạn chế của phương thức xác thực truyền thống chỉ dựa trên Tên đăng nhập và Mật khẩu (Single-Factor Authentication). Khi mật khẩu bị lộ, hệ thống hoàn toàn bất lực nếu không có lớp phân tích ngữ cảnh (Contextual Authentication).
  * Nguy cơ chiếm đoạt tài khoản (**Account Takeover - ATO**) và tấn công đánh cắp phiên (**Session Hijacking**).
* **1.2. Khái niệm và nguyên lý kỹ thuật "Impossible Travel"**:
  * Định nghĩa: Là kỹ thuật phân tích hành vi an ninh (User Behavior Analytics - UBA) nhằm phát hiện hai hoặc nhiều lần đăng nhập của cùng một tài khoản từ hai vị trí địa lý cách xa nhau đến mức người dùng không thể di chuyển vật lý giữa hai nơi đó trong khoảng thời gian tương ứng.
  * Được áp dụng trong các giải pháp SIEM/SOC cao cấp như Microsoft Entra ID Protection, AWS GuardDuty, Okta ThreatInsight.
* **1.3. Mục tiêu và phạm vi của đề tài**:
  * *Mục tiêu*: Xây dựng giải pháp phòng thủ thời gian thực có khả năng tính toán tọa độ, vận tốc di chuyển và tự động ngăn chặn kẻ tấn công khi phát hiện vi phạm Impossible Travel.
  * *Phạm vi*: Tập trung vào tầng ứng dụng web (Web Application Security), sử dụng cơ sở dữ liệu GeoLite2 City offline để tối ưu tốc độ và bảo mật quyền riêng tư.

---

### CHƯƠNG 2: CƠ SỞ LÝ THUYẾT VÀ CÔNG NGHỆ ÁP DỤNG

* **2.1. Kỹ thuật định vị địa lý mạng IP (GeoIP Resolution)**:
  * Cơ chế định tuyến IP và cách các nhà cung cấp dữ liệu (như MaxMind) xây dựng cơ sở dữ liệu ánh xạ từ dải mạng (CIDR) sang Quốc gia, Thành phố, Vĩ độ (Latitude) và Kinh độ (Longitude).
  * Cấu trúc file nhị phân `.mmdb` của MaxMind GeoLite2 City: Tối ưu tra cứu cây nhị phân (Binary Tree Search) với độ trễ cực thấp (< 1ms).
* **2.2. Toán học mặt cầu và Công thức Haversine (Great-Circle Distance)**:
  * Tại sao không thể dùng công thức khoảng cách phẳng Euclid $\sqrt{\Delta x^2 + \Delta y^2}$ (do Trái Đất là hình cầu elip xấp xỉ bán kính $R \approx 6.371\text{ km}$).
  * Công thức Haversine tính khoảng cách cung tròn lớn nhất giữa hai điểm $(\phi_1, \lambda_1)$ và $(\phi_2, \lambda_2)$:
    $$a = \sin^2\left(\frac{\Delta \phi}{2}\right) + \cos(\phi_1) \cdot \cos(\phi_2) \cdot \sin^2\left(\frac{\Delta \lambda}{2}\right)$$
    $$c = 2 \cdot \text{atan2}\left(\sqrt{a}, \sqrt{1 - a}\right)$$
    $$d = R \cdot c$$
* **2.3. Mô hình tính toán vận tốc di chuyển và cơ sở chọn ngưỡng an toàn**:
  * Công thức vận tốc: $v = \frac{d}{\Delta t} \times 3600\text{ (km/h)}$.
  * Cơ sở khoa học của ngưỡng $900\text{ km/h}$: Vận tốc hành trình tối đa của máy bay thương mại chở khách phản lực hiện đại (Boeing 787, Airbus A350) dao động từ $850 - 910\text{ km/h}$. Mọi vận tốc lớn hơn $900\text{ km/h}$ trong khoảng cách > 50 km đều bị coi là vi phạm vật lý đối với người dùng thông thường.
* **2.4. Nguyên lý xác thực an toàn và quản lý phiên làm việc**:
  * Cơ chế BCrypt: Hàm băm thích ứng (Adaptive Hash) có cấu trúc `Salt + Cost Factor`, bảo vệ mật khẩu chống tấn công phần cứng GPU.
  * Quản lý phiên làm việc an toàn (Stateful Session Tracking): Thu hồi phiên tức thời (`is_active = false`), kiểm soát đa phiên đồng thời.
* **2.5. Công nghệ áp dụng**:
  * Backend: Java 21 LTS, Spring Boot 3, Spring Security, Spring Data JPA, Hibernate.
  * Cơ sở dữ liệu: MySQL 8.0 (hỗ trợ lưu trữ độ chính xác cao `DECIMAL(10,6)` cho tọa độ).
  * Frontend: React 18, Vite, Lucide Icons, Vanilla CSS Glassmorphism.

---

### CHƯƠNG 3: PHÂN TÍCH VÀ THIẾT KẾ HỆ THỐNG

* **3.1. Phân tích các mối đe dọa (Threat Modeling)**:
  * Liệt kê các kịch bản kẻ xấu chiếm đoạt phiên (Session Hijacking via Stolen Cookie), chia sẻ tài khoản trái phép, đăng nhập qua VPN hoặc mạng ẩn danh Tor.
* **3.2. Kiến trúc tổng thể hệ thống**:
  * Sơ đồ khối kiến trúc 3 lớp: Presentation Layer (SOC Dashboard) $\leftrightarrow$ Business Logic & Security Engine $\leftrightarrow$ Persistence Layer (MySQL DB & MaxMind GeoLite2).
* **3.3. Thiết kế luồng xử lý 2 kịch bản an ninh cốt lõi**:
  * **Kịch bản 1 (Concurrent Session Anomaly)**: Người dùng đang có phiên hợp lệ tại Vị trí A, nhưng có yêu cầu đăng nhập mới từ Vị trí B rất xa trong thời gian thực ($\Delta t \approx 0$).
    * *Biện pháp an ninh*: Chặn đăng nhập mới (HTTP 403), thu hồi/hủy ngay lập tức phiên cũ để bảo vệ tài khoản, ghi nhận sự kiện mức `CRITICAL`.
  * **Kịch bản 2 (Sequential Impossible Travel)**: Người dùng đã đăng xuất tại Vị trí A, nhưng ngay sau đó đăng nhập tại Vị trí B với khoảng cách lớn trong thời gian quá ngắn ($v > 900\text{ km/h}$).
    * *Biện pháp an ninh*: Chặn truy cập (HTTP 403), không cấp token mới, ghi nhận sự kiện mức `HIGH` hoặc `CRITICAL`.
  * **Kịch bản 3 (Realistic Travel - Hợp lệ)**: Người dùng đăng nhập tại Hà Nội $\rightarrow$ Đăng xuất $\rightarrow$ Sau 2.5 giờ bay máy bay $\rightarrow$ Đăng nhập tại TP.HCM ($1.138\text{ km}$, $v \approx 455\text{ km/h} < 900\text{ km/h}$).
    * *Biện pháp an ninh*: Xác thực thành công (HTTP 200 OK), cấp token phiên làm việc mới.
* **3.4. Thiết kế cơ sở dữ liệu**:
  * Chi tiết 5 bảng: `users`, `user_sessions`, `login_history`, `security_events`, `system_configs`.
  * Trình bày sơ đồ quan hệ thực thể (ERD).
* **3.5. Thiết kế RESTful API**:
  * Bảng đặc tả các Endpoint: `/api/auth/login`, `/api/auth/register`, `/api/auth/logout`, `/api/dashboard/summary`, `/api/security-events`.

---

### CHƯƠNG 4: HIỆN THỰC HÓA VÀ CÀI ĐẶT HỆ THỐNG

* **4.1. Hiện thực hóa phân hệ Xác thực và Quản lý phiên**:
  * Giải thích mã nguồn phương thức `login()` trong [AuthService.java](file:///d:/Phenikaa/Bao_Mat_Ung_Dung_Va_He_Thong/impossible-travel-detection/backend/src/main/java/com/phenikaa/security/impossibletravel/service/AuthService.java).
  * Quy trình tạo mã phiên ngẫu nhiên an toàn (Secure Random Session Token) và cơ chế khóa phiên.
* **4.2. Hiện thực hóa Động cơ phát hiện di chuyển bất khả thi**:
  * Phân tích mã nguồn [ImpossibleTravelEngine.java](file:///d:/Phenikaa/Bao_Mat_Ung_Dung_Va_He_Thong/impossible-travel-detection/backend/src/main/java/com/phenikaa/security/impossibletravel/service/ImpossibleTravelEngine.java).
  * Cách trích xuất IP của phiên đang hoạt động gần nhất và lịch sử đăng nhập thành công gần nhất làm **Mốc đối chiếu (Reference Attempt)**.
* **4.3. Cài đặt thuật toán toán học và dịch vụ GeoIP**:
  * Mã nguồn tính toán [HaversineCalculator.java](file:///d:/Phenikaa/Bao_Mat_Ung_Dung_Va_He_Thong/impossible-travel-detection/backend/src/main/java/com/phenikaa/security/impossibletravel/util/HaversineCalculator.java).
  * Đọc cơ sở dữ liệu `GeoLite2-City.mmdb` với cơ chế Singleton `DatabaseReader` trong [GeoLocationService.java](file:///d:/Phenikaa/Bao_Mat_Ung_Dung_Va_He_Thong/impossible-travel-detection/backend/src/main/java/com/phenikaa/security/impossibletravel/service/GeoLocationService.java).
* **4.4. Hiện thực hóa phân hệ Giám sát SOC**:
  * Cơ chế truy vấn dữ liệu thống kê KPI trong [DashboardService.java](file:///d:/Phenikaa/Bao_Mat_Ung_Dung_Va_He_Thong/impossible-travel-detection/backend/src/main/java/com/phenikaa/security/impossibletravel/service/DashboardService.java).
  * Đóng gói thông tin so sánh đối chiếu giữa Lần đăng nhập mới và Mốc đối chiếu (gồm cả tọa độ, thời gian và địa chỉ IP).
* **4.5. Hiện thực hóa giao diện SOC Dashboard**:
  * Giới thiệu các thành phần giao diện: Thẻ thống kê chỉ số an ninh, Bảng nhật ký hoạt động, Popup thông tin chi tiết sự kiện an ninh.

---

### CHƯƠNG 5: KIỂM THỬ, ĐÁNH GIÁ KẾT QUẢ VÀ THẢO LUẬN

* **5.1. Môi trường kiểm thử**:
  * Cấu hình phần cứng máy chủ thử nghiệm, phiên bản JDK, MySQL, cơ sở dữ liệu MaxMind GeoLite2.
* **5.2. Kết quả kiểm thử các kịch bản thực nghiệm**:
  * *Bảng tổng hợp kết quả kiểm thử*:
    1. Đăng ký & Đăng nhập thành công lần đầu tại Hà Nội (`118.70.128.1`) $\rightarrow$ Thành công (200 OK).
    2. Đăng nhập đồng thời tại New York (`128.122.1.1`) khi phiên Hà Nội đang mở $\rightarrow$ Bị chặn ngay lập tức (403 Forbidden, Severity: CRITICAL), phiên Hà Nội bị hủy.
    3. Đăng xuất ở Hà Nội $\rightarrow$ 5 phút sau đăng nhập ở TP.HCM (`1.53.255.1`) $\rightarrow$ Bị chặn do vận tốc $13.656\text{ km/h} > 900\text{ km/h}$ (403 Forbidden, Severity: HIGH).
    4. Đăng xuất ở Hà Nội $\rightarrow$ 2.5 giờ sau đăng nhập ở TP.HCM $\rightarrow$ Đăng nhập thành công (200 OK, Vận tốc thực tế $\approx 455\text{ km/h} < 900\text{ km/h}$).
    5. Đăng nhập sai mật khẩu $\rightarrow$ Trả về 401 Unauthorized, ghi nhận `status = FAILED`.
* **5.3. Đánh giá độ chính xác và tỷ lệ cảnh báo sai (False Positive / False Negative)**:
  * Trường hợp VPN / Proxy làm sai lệch vị trí IP.
  * Giải pháp của hệ thống: Thiết lập ngưỡng dung sai khoảng cách tối thiểu ($50\text{ km}$) để tránh báo sai giữa các trạm BTS di động trong cùng một thành phố.
* **5.4. Đánh giá hiệu năng hệ thống**:
  * Thời gian xử lý của Engine phát hiện: Thời gian phân giải GeoIP + Tính khoảng cách Haversine chỉ mất trung bình **< 5ms**, không gây trễ đáng kể cho luồng xác thực đăng nhập của người dùng.

---

### CHƯƠNG 6: KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN

* **6.1. Những kết quả đã đạt được**:
  * Xây dựng thành công hệ thống phát hiện Impossible Travel hoạt động theo thời gian thực với độ chính xác cao.
  * Tự động nhận diện và phân loại cả 2 kịch bản vi phạm: Đăng nhập đồng thời và Di chuyển nối tiếp.
  * Xây dựng Dashboard quản trị và giám sát an ninh trực quan chuẩn SOC.
  * Đạt độ bao phủ kiểm thử cao (29/29 Unit & Integration Tests vượt qua 100%).
* **6.2. Hạn chế hiện tại**:
  * Phụ thuộc vào độ chính xác của cơ sở dữ liệu IP GeoLite2 (đặc biệt với mạng 4G/5G khi IP thường tập trung về Gateway trung tâm của nhà mạng).
  * Chưa nhận diện được các địa chỉ IP sử dụng VPN thương mại, Proxy hoặc mạng ẩn danh Tor.
* **6.3. Hướng nghiên cứu và phát triển tiếp theo**:
  * **Tích hợp Adaptive MFA (Xác thực đa yếu tố thích ứng)**: Thay vì chặn hoàn toàn, khi phát hiện rủi ro mức độ vừa phải, hệ thống gửi mã OTP hoặc thông báo xác thực sinh trắc học qua thiết bị di động.
  * **Bổ sung Dấu vân tay thiết bị (Device Fingerprinting)**: Kết hợp phân tích User-Agent, Canvas Fingerprint, múi giờ và độ phân giải màn hình để nâng cao độ tin cậy.
  * **Áp dụng Học máy (Machine Learning)**: Xây dựng hồ sơ hành vi người dùng (User Baseline Profile) để học thói quen di chuyển và lịch trình công tác của từng cá nhân.

---

## TÀI LIỆU THAM KHẢO CHUẨN IEEE

1. R. Sinnott, "The Haversine Formula," *Sky and Telescope*, vol. 68, no. 2, p. 159, 1984.
2. MaxMind Inc., "GeoLite2 Free Geolocation Data," MaxMind Developer Portal, 2026. [Online]. Available: https://dev.maxmind.com/geoip/geolite2-free-geolocation-data
3. NIST, "Digital Identity Guidelines: Authentication and Lifecycle Management," *NIST Special Publication 800-63B*, National Institute of Standards and Technology, 2020.
4. Microsoft Corporation, "What is Impossible Travel Risk in Microsoft Entra ID Protection?", Microsoft Security Documentation, 2024.
5. OWASP Foundation, "Credential Stuffing Prevention Cheat Sheet," OWASP Cheat Sheet Series, 2024. [Online]. Available: https://cheatsheetseries.owasp.org
