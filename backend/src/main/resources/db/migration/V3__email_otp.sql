CREATE TABLE IF NOT EXISTS email_challenges (
    id VARCHAR(36) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    purpose VARCHAR(20) NOT NULL,
    email VARCHAR(254) NOT NULL,
    code_hash VARCHAR(255) NOT NULL,
    status VARCHAR(24) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    last_sent_at TIMESTAMP(6) NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    send_count INT NOT NULL DEFAULT 0,
    security_event_id BIGINT NULL,
    ip_address VARCHAR(45),
    city VARCHAR(255), country VARCHAR(255), latitude DOUBLE, longitude DOUBLE,
    CONSTRAINT fk_email_challenge_user FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_email_challenge_user (user_id, created_at)
);
