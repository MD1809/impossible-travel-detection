package com.phenikaa.security.impossibletravel.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "email_challenges", indexes = @Index(name = "idx_email_challenge_user", columnList = "user_id,created_at"))
@Getter @Setter
public class EmailChallenge {
    @Id @Column(length = 36) private String id;
    @Column(name = "user_id", nullable = false) private Long userId;
    @Column(nullable = false, length = 20) private String purpose;
    @Column(nullable = false, length = 254) private String email;
    @Column(name = "code_hash", nullable = false) private String codeHash;
    @Column(nullable = false, length = 24) private String status;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
    @Column(name = "last_sent_at", nullable = false) private Instant lastSentAt;
    private int attempts;
    @Column(name = "send_count") private int sendCount;
    @Column(name = "security_event_id") private Long securityEventId;
    @Column(name = "ip_address", length = 45) private String ipAddress;
    private String city;
    private String country;
    private Double latitude;
    private Double longitude;
}
