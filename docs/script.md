# KỊCH BẢN THUYẾT TRÌNH BẢO VỆ ĐỒ ÁN (PRESENTATION SCRIPT)

* **Tên đề tài**: Nghiên cứu và Xây dựng Hệ thống Phát hiện Đăng nhập Bất thường theo Khoảng cách Địa lý (Impossible Travel Detection System)
* **Học phần**: Bảo Mật Ứng Dụng Và Hệ Thống — Khoa CNTT, Đại Học Phenikaa
* **Thời lượng thuyết trình chuẩn**: 12 – 15 phút (gồm Slide + Live Demo + Vấn đáp)
* **Phân vai trình bày**:
  * **Thành viên 1 (TV1)**: Mở đầu, Đặt vấn đề, Quản lý Định danh & Phiên (IAM), Kịch bản kiểm thử, Kết luận.
  * **Thành viên 2 (TV2)**: Thuật toán Haversine, Cơ sở dữ liệu GeoIP, Động cơ Impossible Travel Engine, Trực tiếp Live Demo.

---

## TỔNG QUAN PHÂN BỔ THỜI GIAN

| Phần | Nội dung | Người nói | Thời lượng |
| :---: | :--- | :---: | :---: |
| **Phần 1** | Giới thiệu, Đặt vấn đề & Khái niệm Impossible Travel | **Thành viên 1** | 2.5 phút |
| **Phần 2** | Cơ sở Toán học (Haversine), GeoIP & Động cơ Phát hiện | **Thành viên 2** | 3.5 phút |
| **Phần 3** | Kiến trúc Hệ thống & Cơ chế Quản lý Phiên An toàn | **Thành viên 1** | 2.5 phút |
| **Phần 4** | **TRÌNH DIỄN THỰC NGHIỆM (LIVE DEMO - 4 KỊCH BẢN)** | **Thành viên 2** (phối hợp TV1) | 4.0 phút |
| **Phần 5** | Đánh giá, Hướng phát triển & Kết luận | **Thành viên 1** | 1.5 phút |
| **Phần 6** | **Hỏi đáp và Vấn đáp với Hội đồng Giảng viên (Q&A)** | **Cả hai thành viên** | 5.0 phút |

---

## PHẦN 1: MỞ ĐẦU & ĐẶT VẤN ĐỀ (00:00 – 02:30)
*(Người trình bày: **Thành viên 1**)*

### Slide 1: Trang Tiêu Đề
* **Hành động**: Đứng tự tin, chào Hội đồng giảng viên và các bạn sinh viên.
* **Lời thoại**:
  > *"Kính chào quý Thầy Cô trong Hội đồng phản biện và các bạn sinh viên. Hôm nay, nhóm em gồm 2 thành viên: [Tên bạn 1] và [Tên bạn 2], xin phép được đại diện nhóm bảo vệ đồ án môn học **Bảo mật Ứng dụng và Hệ thống** với đề tài: **'Nghiên cứu và Xây dựng Hệ thống Phát hiện Đăng nhập Bất thường theo Khoảng cách Địa lý - Impossible Travel Detection'** dưới sự hướng dẫn của [Tên Giảng viên]. Sau đây em xin phép được bắt đầu phần trình bày."*

---

### Slide 2: Đặt Vấn Đề & Động Lực Nghiên Cứu
* **Hành động**: Chuyển slide, nhấn mạnh vào điểm yếu của phương thức xác thực truyền thống.
* **Lời thoại**:
  > *"Thưa Thầy Cô, trong bối cảnh các cuộc tấn công mạng ngày càng tinh vi, việc người dùng bị lộ lọt mật khẩu qua các vụ rò rỉ dữ liệu hoặc mã độc đánh cắp tài khoản (Credential Stuffing / Account Takeover) là thực tế diễn ra hàng ngày.*
  >
  > *Tuy nhiên, phần lớn các hệ thống web hiện nay chỉ thực hiện cơ chế xác thực truyền thống: kiểm tra xem Tên đăng nhập và Mật khẩu có khớp trong cơ sở dữ liệu hay không. Hệ thống **hoàn toàn mù mờ về ngữ cảnh (Context-unaware)**. Nếu một sinh viên đang làm việc tại Hà Nội, chỉ 5 phút sau lại có người dùng đúng mật khẩu đó đăng nhập từ New York hay Tokyo, hệ thống truyền thống vẫn mở cửa đón kẻ tấn công.*
  >
  > *Đây chính là động lực để nhóm em nghiên cứu và phát triển giải pháp phát hiện xâm nhập dựa trên phân tích hành vi di chuyển bất khả thi."*

---

### Slide 3: Khái Niệm "Impossible Travel" Là Gì?
* **Hành động**: Chỉ vào công thức toán học và biểu tượng máy bay trên slide.
* **Lời thoại**:
  > *"Vậy, thế nào là **Impossible Travel**?*
  >
  > *Hiểu một cách đơn giản, đây là tình trạng một tài khoản xuất hiện tại hai vị trí địa lý khác nhau trong một khoảng thời gian ngắn đến mức con người không thể di chuyển vật lý giữa hai địa điểm đó.*
  >
  > *Nguyên lý cốt lõi dựa trên công thức vật lý cơ bản: **Vận tốc $v = d / \Delta t$** (Khoảng cách chia Thời gian chênh lệch).*
  > * *Nhóm em lấy mốc trần là **$900\text{ km/h}$** — đây là vận tốc hành trình tối đa của các dòng máy bay thương mại chở khách hiện đại như Boeing 787 hay Airbus A350.*
  > * *Nếu vận tốc tính toán vượt quá $900\text{ km/h}$, chắc chắn có sự bất thường và hệ thống sẽ tự động kích hoạt cơ chế phòng thủ.*
  > * *Đồng thời, nhóm cũng đặt một ngưỡng dung sai nội đô là $50\text{ km}$ để người dùng chuyển mạng 4G/Wi-Fi trong cùng thành phố không bị báo sai."*

---

### Slide 4: Mục Tiêu Đề Tài & Chuyển Giao Lời
* **Lời thoại**:
  > *"Mục tiêu của nhóm là xây dựng một hệ thống phòng thủ chủ động: Tính toán và ra quyết định chặn ngay trong vòng chưa đầy $5\text{ms}$ khi người dùng đăng nhập, thu hồi phiên làm việc khi có xung đột và cung cấp SOC Dashboard giám sát trực quan.*
  >
  > *Để hiểu rõ hơn về nền tảng toán học và động cơ tính toán Impossible Travel Engine, em xin mời bạn [Tên bạn 2] tiếp tục phần trình bày."*

---

## PHẦN 2: CƠ SỞ TOÁN HỌC, GEOIP & ĐỘNG CƠ PHÁT HIỆN (02:30 – 06:00)
*(Người trình bày: **Thành viên 2**)*

### Slide 5: Nền Tảng Thuật Toán Haversine & MaxMind GeoLite2
* **Hành động**: Tiếp nhận phần trình bày, bước lên vị trí trung tâm, giọng rành mạch.
* **Lời thoại**:
  > *"Em xin cảm ơn bạn [Tên bạn 1]. Kính thưa Thầy Cô, để hiện thực hóa việc tính toán vận tốc trong không gian thực, hệ thống phải giải quyết hai bài toán then chốt: **Định vị IP** và **Đo khoảng cách mặt cầu Trái Đất**.*
  >
  > * **Thứ nhất - Về định vị địa lý**: Nhóm tích hợp cơ sở dữ liệu **MaxMind GeoLite2 City**. Thay vì gọi API ra bên thứ ba gây trễ mạng và lo ngại rò rỉ dữ liệu, file nhị phân `.mmdb` được nhúng trực tiếp và nạp vào bộ nhớ RAM của máy chủ Backend. Thời gian phân giải từ một địa chỉ IP sang Tọa độ (Vĩ độ, Kinh độ) đạt tốc độ cực nhanh: **chưa tới $1\text{ms}$**.*
  >
  > * **Thứ hai - Về tính khoảng cách**: Vì Trái Đất là một khối cầu elip elipsoid xấp xỉ bán kính $R = 6.371\text{ km}$, nếu dùng công thức mặt phẳng Euclide sẽ sai số rất lớn. Nhóm em áp dụng **Công thức lượng giác mặt cầu Haversine** để tính khoảng cách cung tròn lớn nhất (Great-Circle Distance) giữa hai cặp tọa độ $(\phi_1, \lambda_1)$ và $(\phi_2, \lambda_2)$ với độ chính xác cao."*

---

### Slide 6: Hai Kịch Bản An Ninh Cốt Lõi (Detection Engine)
* **Hành động**: Dùng tay chỉ rõ sự khác biệt giữa 2 kịch bản trên hình vẽ.
* **Lời thoại**:
  > *"Tại tầng lõi của hệ thống, lớp `ImpossibleTravelEngine` được thiết kế để phân tích và ngăn chặn **2 kịch bản tấn công thực tế**:*
  >
  > * **Kịch bản 1: Concurrent Session Anomaly (Xung đột phiên đồng thời)**:*
  >   * *Nạn nhân đang có một phiên làm việc hợp lệ tại Hà Nội.*
  >   * *Hacker có mật khẩu, đăng nhập từ New York ngay thời điểm đó.*
  >   * *Vì khoảng cách hơn $13.000\text{ km}$ mà thời gian giữa 2 phiên $\Delta t \approx 0$, hệ thống xác định đây là vi phạm mức **CRITICAL**. Hành động: **Chặn yêu cầu của hacker**, đồng thời **lập tức thu hồi (hủy) luôn phiên của nạn nhân** để bảo vệ tài khoản.*
  >
  > * **Kịch bản 2: Sequential Impossible Travel (Di chuyển nối tiếp bất khả thi)**:*
  >   * *Người dùng vừa đăng xuất ở Hà Nội lúc 08:00.*
  >   * *5 phút sau (08:05), xuất hiện đăng nhập ở TP.HCM ($1.138\text{ km}$).*
  >   * *Vận tốc tính ra là hơn $13.600\text{ km/h} > 900\text{ km/h}$. Hệ thống từ chối cấp token mới, ghi log cảnh báo mức **HIGH**.*
  >
  > *Tiếp theo, bạn [Tên bạn 1] sẽ trình bày về kiến trúc tổng thể và cơ chế bảo mật phiên của hệ thống."*

---

## PHẦN 3: KIẾN TRÚC HỆ THỐNG & BẢO MẬT PHIÊN (06:00 – 08:30)
*(Người trình bày: **Thành viên 1**)*

### Slide 7: Kiến Trúc 3 Lớp & Cơ Chế Quản Lý Phiên (IAM)
* **Lời thoại**:
  > *"Kính thưa Thầy Cô, hệ thống được xây dựng theo kiến trúc 3 lớp phân tách rõ ràng:*
  > * *Tầng Giao diện (Presentation Layer): Xây dựng SOC Dashboard bằng React để trực quan hóa toàn bộ sự kiện an ninh.*
  > * *Tầng Xử lý Nghiệp vụ & Động cơ Bảo mật: Viết bằng Java 21, Spring Boot 3, tích hợp Spring Security.*
  > * *Tầng Dữ liệu: CSDL quan hệ MySQL 8.0 lưu trữ lịch sử xác thực và MaxMind GeoLite2.*
  >
  > *Đặc biệt, về mặt an ninh dữ liệu:*
  > * *Toàn bộ mật khẩu người dùng được băm một chiều bằng thuật toán **BCrypt** với chuỗi muối ngẫu nhiên (Random Salt) và Cost Factor chuẩn công nghiệp, chống hoàn toàn các cuộc tấn công bảng băm cầu vồng (Rainbow Table).*
  > * *Các phiên làm việc được quản lý theo mô hình **Stateful Secure Session** trong bảng `user_sessions`. Khi phát hiện vi phạm, hàm `invalidateSession()` có thể thu hồi quyền truy cập của token chỉ trong một truy vấn duy nhất."*

---

## PHẦN 4: TRÌNH DIỄN THỰC NGHIỆM (LIVE DEMO) (08:30 – 12:30)
*(Người thực hiện: **Thành viên 2** thao tác màn hình, **Thành viên 1** thuyết minh phối hợp)*

* **Chuẩn bị màn hình**: Mở sẵn trình duyệt ở tab Dashboard (`http://localhost:5173`) và cửa sổ Postman.

---

### Kịch bản Demo 1: Đăng nhập bình thường tại Hà Nội (Baseline)
* **Thao tác**: Bấm gửi Request Đăng nhập tại Hà Nội (`ipAddress: 118.70.128.1`).
* **Lời thoại (TV2)**:
  > *"Đầu tiên, em sẽ mô phỏng sinh viên `student01` đăng nhập từ địa chỉ IP tại Hà Nội. Kết quả trả về ngay lập tức là **200 OK**, hệ thống cấp Session Token hợp lệ. Trên SOC Dashboard, chỉ số Active Sessions tăng lên 1 và nhật ký hiển thị vị trí Hà Nội màu xanh an toàn."*

---

### Kịch bản Demo 2: Tấn công phiên đồng thời từ New York (Concurrent Anomaly)
* **Thao tác**: Khi phiên Hà Nội đang sống, gửi Request đăng nhập từ New York (`ipAddress: 128.122.1.1`).
* **Lời thoại (TV1 & TV2)**:
  > *(TV2 thao tác)*: *"Bây giờ, kẻ tấn công chiếm được tài khoản và cố tình đăng nhập từ New York trong khi phiên ở Hà Nội vẫn đang hoạt động.*
  >
  > *(TV1 thuyết minh)*: *Ngay lập tức, hệ thống phản hồi **HTTP 403 Forbidden**. Trên Dashboard, sự kiện màu đỏ rực mức **CRITICAL** lập tức xuất hiện. Khi em bấm vào xem chi tiết: Hệ thống hiển thị rõ ràng cả 2 mốc:*
  > * *Lần đăng nhập mới: New York, IP `128.122.1.1`.*
  > * *Mốc đối chiếu: Hà Nội, IP `118.70.128.1`.*
  > * *Khoảng cách tính toán: $13.149\text{ km}$, chênh lệch $0\text{ giây}$.*
  > * *Đồng thời, phiên làm việc ở Hà Nội đã bị hệ thống tự động hủy để kẻ xấu không thể lợi dụng."*

---

### Kịch bản Demo 3: Di chuyển nối tiếp bất khả thi Hà Nội $\rightarrow$ TP.HCM (Sequential Travel)
* **Thao tác**: Đăng xuất sạch phiên $\rightarrow$ Gửi Request đăng nhập tại Hà Nội $\rightarrow$ Ngay sau đó gửi đăng nhập tại TP.HCM (`1.53.255.1`).
* **Lời thoại (TV2)**:
  > *"Tiếp theo là kịch bản người dùng đã đăng xuất ở Hà Nội, nhưng chỉ 5 phút sau lại xuất hiện tại TP. Hồ Chí Minh. Khoảng cách giữa 2 thành phố là khoảng $1.138\text{ km}$. Hệ thống tính ra vận tốc yêu cầu lên tới $13.653\text{ km/h}$. Do vượt ngưỡng $900\text{ km/h}$, yêu cầu đăng nhập lập tức bị từ chối với mã 403 Forbidden."*

---

### Kịch bản Demo 4: Di chuyển hợp lệ bằng đường hàng không (Realistic Travel)
* **Thao tác**: Gửi đăng nhập tại Hà Nội lúc 06:00 $\rightarrow$ Đăng xuất $\rightarrow$ Gửi đăng nhập tại TP.HCM với mốc thời gian 08:30 (sau 2.5 tiếng bay).
* **Lời thoại (TV1 & TV2)**:
  > *(TV1)*: *"Vậy câu hỏi đặt ra là: Nếu người dùng thực sự đi máy bay từ Hà Nội vào TP.HCM thì sao?*
  >
  > *(TV2 thao tác)*: *Em mô phỏng người dùng bay chuyến bay sáng mất 2 giờ 30 phút. Khoảng cách $1.138\text{ km}$ chia cho 2.5 giờ tương đương vận tốc **$455\text{ km/h}$** — hoàn toàn nằm trong giới hạn máy bay thương mại ($< 900\text{ km/h}$).*
  >
  > *(TV1)*: *Như Thầy Cô thấy trên màn hình: Kết quả trả về **200 OK Đăng nhập thành công!** Điều này chứng minh hệ thống không hề chặn bừa bãi theo IP mà tính toán chính xác theo quy luật vật lý thực tế."*

---

## PHẦN 5: ĐÁNH GIÁ, HƯỚNG PHÁT TRIỂN & KẾT LUẬN (12:30 – 14:00)
*(Người trình bày: **Thành viên 1**)*

### Slide Kết Luận & Hướng Phát Triển
* **Lời thoại**:
  > *"Kính thưa Thầy Cô, qua quá trình nghiên cứu và thực nghiệm, nhóm đã đạt được các kết quả chính:*
  > 1. *Xây dựng thành công động cơ phát hiện Impossible Travel hoạt động theo thời gian thực với độ trễ xử lý cực thấp ($< 5\text{ms}$).*
  > 2. *Bảo vệ tài khoản toàn diện trước cả hai hình thức: Xung đột phiên đồng thời và Di chuyển nối tiếp bất khả thi.*
  > 3. *Kiểm thử tự động 29/29 bài test đều vượt qua 100%.*
  >
  > *Về hướng phát triển trong tương lai, nhóm định hướng:*
  > * *Tích hợp cơ chế **Adaptive MFA**: Thay vì chặn cứng, hệ thống sẽ gửi OTP hoặc sinh trắc học khi rủi ro ở mức vừa phải.*
  > * *Bổ sung **Dấu vân tay thiết bị (Device Fingerprinting)** để tránh báo nhầm khi người dùng sử dụng mạng VPN công ty.*
  >
  > *Nhóm em xin chân thành cảm ơn quý Thầy Cô đã chú ý lắng nghe. Chúng em rất mong nhận được những câu hỏi và ý kiến đóng góp từ Hội đồng ạ!"*

---

## PHẦN 6: BỘ CÂU HỎI VẤN ĐÁP DỰ KIẾN TỪ HỘI ĐỒNG (FAQ & ANSWERS)

> [!TIP]
> Dưới đây là 5 câu hỏi "kinh điển" mà các giảng viên bảo mật thường hỏi, kèm câu trả lời chuẩn xác nhất để 2 bạn tự tin đạt điểm tối đa:

### Câu hỏi 1: *"Nếu người dùng dùng VPN hoặc mạng 4G/5G đổi IP liên tục thì hệ thống có chặn nhầm không?"*
* **Người trả lời đề xuất**: **Thành viên 2**
* **Câu trả lời chuẩn**:
  > *"Dạ thưa Thầy/Cô, đây là thách thức phổ biến của giải pháp GeoIP. Nhóm đã áp dụng 2 giải pháp:*
  > * *Thứ nhất: Ngưỡng lọc nội đô $50\text{ km}$. Nếu người dùng đổi trạm BTS 4G/5G trong cùng tỉnh/thành phố, khoảng cách $< 50\text{ km}$ sẽ được bỏ qua.*
  > * *Thứ hai: Với VPN xuyên quốc gia, về bản chất an ninh mạng doanh nghiệp (như chính sách của Microsoft Entra ID hay Google), việc IP nhảy tức thời sang nước khác luôn bị xếp vào diện rủi ro cao (High Risk). Trong tương lai, hệ thống sẽ tích hợp danh sách VPN/Tor IP Database để nhận diện và kích hoạt xác thực đa yếu tố MFA thay vì chặn hoàn toàn ạ."*

---

### Câu hỏi 2: *"Cơ chế nào giúp hệ thống tính toán khoảng cách và đưa ra quyết định nhanh như vậy mà không làm nghẽn đăng nhập?"*
* **Người trả lời đề xuất**: **Thành viên 2**
* **Câu trả lời chuẩn**:
  > *"Dạ thưa Thầy/Cô, tốc độ đạt được là nhờ nhóm không dùng Web API ngoài mà nạp trực tiếp file nhị phân MaxMind `GeoLite2-City.mmdb` vào RAM qua đối tượng `DatabaseReader` dạng Singleton. Phân giải IP bằng cây nhị phân chỉ mất $< 1\text{ms}$. Công thức Haversine thuần túy là các phép toán lượng giác cơ bản trong CPU, nên tổng thời gian xử lý chỉ mất khoảng $3 - 5\text{ms}$, hoàn toàn không gây trễ cho người dùng ạ."*

---

### Câu hỏi 3: *"Tại sao khi phát hiện phiên đồng thời (Kịch bản 1), hệ thống lại hủy luôn cả phiên đang hoạt động của người dùng?"*
* **Người trả lời đề xuất**: **Thành viên 1**
* **Câu trả lời chuẩn**:
  > *"Dạ thưa Thầy/Cô, đây là nguyên lý phòng thủ chủ động (Zero Trust). Khi xuất hiện 2 phiên ở 2 nơi cách xa nhau cùng lúc, hệ thống không thể biết chắc phiên nào là của người dùng thật, phiên nào là hacker đã đánh cắp token/cookie. Vì vậy, giải pháp an toàn nhất là lập tức vô hiệu hóa phiên cũ (`is_active = false`) và buộc người dùng phải xác thực lại danh tính từ đầu để bảo toàn tài khoản ạ."*

---

### Câu hỏi 4: *"Cơ sở dữ liệu của các bạn lưu trữ mật khẩu và thông tin sự kiện an ninh như thế nào để đảm bảo tính toàn vẹn?"*
* **Người trả lời đề xuất**: **Thành viên 1**
* **Câu trả lời chuẩn**:
  > *"Dạ thưa Thầy/Cô: Mật khẩu được mã hóa một chiều bằng BCrypt với Salt ngẫu nhiên, không thể giải mã ngược lại kể cả khi lộ CSDL. Còn bảng `security_events` và `login_history` được thiết kế theo nguyên tắc Append-Only (chỉ thêm mới, không cho phép sửa đổi thời gian và tọa độ), lưu đầy đủ IP, vị trí và ID sự kiện để phục vụ việc điều tra số (Forensics) không thể chối bỏ ạ."*

---

### Câu hỏi 5: *"Tại sao nhóm lại chọn ngưỡng vận tốc là 900 km/h mà không phải con số khác?"*
* **Người trả lời đề xuất**: **Thành viên 2**
* **Câu trả lời chuẩn**:
  > *"Dạ thưa Thầy/Cô, $900\text{ km/h}$ là vận tốc hành trình thực tế của các dòng máy bay phản lực thương mại dân dụng phổ biến nhất hiện nay (khoảng Mach 0.82 - 0.85). Đây cũng là thông số tiêu chuẩn được các hãng bảo mật lớn như Microsoft và Okta áp dụng trong thuật toán Impossible Travel để cân bằng giữa bảo mật và trải nghiệm người dùng, hạn chế tối đa tình trạng cảnh báo sai (False Positive) ạ."*
