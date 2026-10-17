-- Migration V2: Mở rộng bảng login_history thành Nhật ký hoạt động toàn diện
-- Cho phép lưu cả các trường hợp: Đăng nhập thành công, Đăng xuất, Sai tài khoản (user_id null), Sai mật khẩu và Bị chặn

-- 1. Cho phép user_id là NULL khi đăng nhập sai tên tài khoản không tồn tại
ALTER TABLE login_history MODIFY COLUMN user_id BIGINT NULL;

-- 2. Bổ sung attempted_username để lưu tên tài khoản người dùng nhập
ALTER TABLE login_history ADD COLUMN attempted_username VARCHAR(100) NULL AFTER user_id;

-- 3. Mở rộng cột status sang VARCHAR(20) để lưu linh hoạt các trạng thái: SUCCESS, FAILED, BLOCKED, LOGOUT
ALTER TABLE login_history MODIFY COLUMN status VARCHAR(20) NOT NULL;
