-- ====================================================================
-- Script khởi tạo cơ sở dữ liệu cho đề tài Impossible Travel Detection
-- Đối chiếu: docs/03_DATABASE_SCHEMA.md
-- ====================================================================

-- 1. Bảng users: Quản lý người dùng
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Bảng user_sessions: Phiên làm việc (phục vụ Kịch bản 1: Concurrent Login Anomaly)
CREATE TABLE IF NOT EXISTS user_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_token VARCHAR(255) NOT NULL UNIQUE,
    ip_address VARCHAR(45) NOT NULL,
    city VARCHAR(100),
    country VARCHAR(100),
    latitude DECIMAL(10, 6) NOT NULL,
    longitude DECIMAL(10, 6) NOT NULL,
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_sessions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_active (user_id, is_active),
    INDEX idx_session_token (session_token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Bảng login_history: Nhật ký hoạt động (đăng nhập, đăng xuất, lỗi đăng nhập, bị chặn)
CREATE TABLE IF NOT EXISTS login_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    attempted_username VARCHAR(100),
    ip_address VARCHAR(45) NOT NULL,
    city VARCHAR(100),
    country VARCHAR(100),
    latitude DECIMAL(10, 6) NOT NULL,
    longitude DECIMAL(10, 6) NOT NULL,
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL,
    failure_reason VARCHAR(255),
    CONSTRAINT fk_history_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_status_time (user_id, status, login_time DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Bảng security_events: Sự kiện cảnh báo an ninh hiển thị trên SOC Dashboard
CREATE TABLE IF NOT EXISTS security_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    city VARCHAR(100),
    country VARCHAR(100),
    latitude DECIMAL(10, 6) NOT NULL,
    longitude DECIMAL(10, 6) NOT NULL,
    ref_city VARCHAR(100),
    ref_country VARCHAR(100),
    ref_latitude DECIMAL(10, 6),
    ref_longitude DECIMAL(10, 6),
    ref_ip VARCHAR(45) NULL,
    ref_time TIMESTAMP NULL,
    distance_km DECIMAL(10, 2),
    time_diff_minutes DECIMAL(10, 2),
    calculated_speed_kmh DECIMAL(10, 2),
    threshold_speed_kmh DECIMAL(10, 2) DEFAULT 900.00,
    severity ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'HIGH',
    action_taken VARCHAR(50) DEFAULT 'BLOCKED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_events_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_created_at (created_at DESC),
    INDEX idx_event_severity (severity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Bảng system_configs: Cấu hình tham số ngưỡng hệ thống
CREATE TABLE IF NOT EXISTS system_configs (
    config_key VARCHAR(50) PRIMARY KEY,
    config_value VARCHAR(255) NOT NULL,
    description VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dữ liệu cấu hình ban đầu
INSERT INTO system_configs (config_key, config_value, description) VALUES 
('SPEED_THRESHOLD_KMH', '900', 'Ngưỡng tốc độ di chuyển tối đa cho phép (km/h)'),
('SESSION_TIMEOUT_MINUTES', '120', 'Thời gian hết hạn của phiên làm việc (phút)'),
('MIN_DISTANCE_KM_THRESHOLD', '50', 'Khoảng cách tối thiểu bắt đầu áp dụng kiểm tra tốc độ (km)')
ON DUPLICATE KEY UPDATE 
    config_value = VALUES(config_value),
    description = VALUES(description);
