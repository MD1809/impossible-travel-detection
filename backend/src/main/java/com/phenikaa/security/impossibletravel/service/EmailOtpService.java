package com.phenikaa.security.impossibletravel.service;

import com.phenikaa.security.impossibletravel.entity.*;
import com.phenikaa.security.impossibletravel.repository.*;
import com.phenikaa.security.impossibletravel.dto.response.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class EmailOtpService {
    private final EmailChallengeRepository challenges;
    private final UserRepository users;
    private final SecurityEventRepository events;
    private final OtpMailService mail;
    private final PasswordEncoder encoder;
    private final LoginHistoryRepository histories;
    private final SecureRandom random = new SecureRandom();

    public EmailOtpService(EmailChallengeRepository challenges, UserRepository users, SecurityEventRepository events,
            OtpMailService mail, PasswordEncoder encoder, LoginHistoryRepository histories) {
        this.challenges = challenges;
        this.users = users;
        this.events = events;
        this.mail = mail;
        this.encoder = encoder;
        this.histories = histories;
    }

    public static LoginResponseDto error(int status, String message) {
        var response = LoginResponseDto.unauthorized(message);
        response.setStatus(status);
        return response;
    }

    public LoginResponseDto issue(User user, String purpose, GeoLocationDto location, Long eventId) {
        users.lockById(user.getId()).orElseThrow();
        Instant now = Instant.now();
        var latest = challenges.findFirstByUserIdOrderByCreatedAtDesc(user.getId());
        if (latest.isPresent() && purpose.equals(latest.get().getPurpose())
                && latest.get().getLastSentAt().plusSeconds(60).isAfter(now))
            return error(429, "Vui lòng chờ 60 giây giữa các lần gửi mã.");
        if (challenges.countByUserIdAndCreatedAtAfter(user.getId(), now.minusSeconds(3600)) >= 5)
            return error(429, "Đã đạt giới hạn yêu cầu xác minh. Vui lòng thử lại sau.");
        var challenge = new EmailChallenge();
        challenge.setId(UUID.randomUUID().toString());
        challenge.setUserId(user.getId());
        challenge.setPurpose(purpose);
        challenge.setEmail(user.getEmail());
        challenge.setCreatedAt(now);
        challenge.setSecurityEventId(eventId);
        if (location != null) {
            challenge.setIpAddress(location.getIpAddress());
            challenge.setCity(location.getCity());
            challenge.setCountry(location.getCountry());
            challenge.setLatitude(location.getLatitude());
            challenge.setLongitude(location.getLongitude());
        }
        if (!send(challenge, now))
            return error(503, "Chưa gửi được email xác minh. Vui lòng thử lại sau.");
        for (var old : challenges.findByUserIdAndStatus(user.getId(), "PENDING")) {
            finish(old, old.getExpiresAt().isAfter(now) ? "SUPERSEDED" : "EXPIRED");
        }
        challenges.save(challenge);
        audit(challenge, "OTP_PENDING");
        return pending(challenge);
    }

    private boolean send(EmailChallenge challenge, Instant now) {
        String code;
        do {
            code = String.format("%06d", random.nextInt(1_000_000));
        } while (challenge.getCodeHash() != null
                && encoder.matches(challenge.getId() + ":" + code, challenge.getCodeHash()));
        String hash = encoder.encode(challenge.getId() + ":" + code);
        try {
            mail.send(challenge.getEmail(), code, "EMAIL_VERIFY".equals(challenge.getPurpose()),
                    challenge.getIpAddress());
        } catch (RuntimeException ex) {
            return false;
        } // No code or SMTP credentials in logs/responses.
        challenge.setCodeHash(hash);
        challenge.setStatus("PENDING");
        challenge.setExpiresAt(now.plusSeconds(300));
        challenge.setLastSentAt(now);
        challenge.setSendCount(challenge.getSendCount() + 1);
        return true;
    }

    private EmailChallenge lock(String id) {
        if (id == null || id.length() != 36)
            return null;
        var userId = challenges.findUserIdByChallengeId(id).orElse(null);
        if (userId == null)
            return null;
        // Read only the owner ID before locking, so JPA cannot cache a stale PENDING
        // entity.
        users.lockById(userId).orElseThrow(); // Same lock order for issue, verify, resend and cancel.
        return challenges.lockById(id).orElse(null);
    }

    private LoginResponseDto unavailable(EmailChallenge challenge) {
        if (challenge == null || !"PENDING".equals(challenge.getStatus()))
            return error(400, "Yêu cầu xác minh không còn hiệu lực. Vui lòng đăng nhập hoặc đăng ký lại.");
        if (!challenge.getExpiresAt().isAfter(Instant.now())) {
            finish(challenge, "EXPIRED");
            return error(400, "Mã đã hết hạn. Vui lòng đăng nhập hoặc đăng ký lại.");
        }
        return null;
    }

    public record Decision(EmailChallenge challenge, LoginResponseDto error) {
    }

    public Decision verify(String id, String code) {
        var challenge = lock(id);
        var invalid = unavailable(challenge);
        if (invalid != null)
            return new Decision(null, invalid);
        var user = users.findById(challenge.getUserId()).orElseThrow();
        if (!Boolean.TRUE.equals(user.getIsActive()) || !challenge.getEmail().equals(user.getEmail())
                || ("LOGIN".equals(challenge.getPurpose()) && !user.isEmailVerified())) {
            finish(challenge, "CANCELLED");
            return new Decision(null, error(403, "Tài khoản không đủ điều kiện xác minh."));
        }
        challenge.setAttempts(challenge.getAttempts() + 1);
        if (code == null || !code.matches("[0-9]{6}") || !encoder.matches(id + ":" + code, challenge.getCodeHash())) {
            if (challenge.getAttempts() >= 5)
                finish(challenge, "LOCKED");
            return new Decision(null, error(challenge.getAttempts() >= 5 ? 429 : 400,
                    challenge.getAttempts() >= 5 ? "Đã nhập sai 5 lần. Yêu cầu xác minh đã bị khóa."
                            : "Mã xác minh không chính xác."));
        }
        finish(challenge, "VERIFIED");
        return new Decision(challenge, null);
    }

    public LoginResponseDto resend(String id) {
        var challenge = lock(id);
        var invalid = unavailable(challenge);
        if (invalid != null)
            return invalid;
        var user = users.findById(challenge.getUserId()).orElseThrow();
        if (!Boolean.TRUE.equals(user.getIsActive()) || !challenge.getEmail().equals(user.getEmail())) {
            finish(challenge, "CANCELLED");
            return error(403, "Tài khoản không đủ điều kiện xác minh.");
        }
        Instant now = Instant.now();
        if (challenge.getLastSentAt().plusSeconds(60).isAfter(now))
            return error(429, "Vui lòng chờ 60 giây trước khi gửi lại mã.");
        if (challenge.getSendCount() >= 3)
            return error(429, "Mỗi yêu cầu chỉ được gửi mã tối đa 3 lần.");
        if (!send(challenge, now))
            return error(503, "Chưa gửi được email. Mã trước vẫn giữ nguyên hiệu lực.");
        return pending(challenge);
    }

    public void cancel(String id) {
        var challenge = lock(id);
        if (challenge != null && "PENDING".equals(challenge.getStatus()))
            finish(challenge, "CANCELLED");
    }

    private void finish(EmailChallenge challenge, String status) {
        challenge.setStatus(status);
        audit(challenge, "OTP_" + status);
        recordExpiredLogin(challenge);
    }

    public void finalizeExpiredLogin(String id) {
        var challenge = lock(id);
        if (challenge == null || challenge.getExpiresAt().isAfter(Instant.now())
                || "VERIFIED".equals(challenge.getStatus()))
            return;
        if ("PENDING".equals(challenge.getStatus()))
            finish(challenge, "EXPIRED");
        else
            recordExpiredLogin(challenge);
    }

    private void recordExpiredLogin(EmailChallenge challenge) {
        if (!"LOGIN".equals(challenge.getPurpose()) || "VERIFIED".equals(challenge.getStatus())
                || challenge.getExpiresAt().isAfter(Instant.now()) || challenge.getSecurityEventId() == null
                || histories.existsBySecurityEventIdAndStatus(challenge.getSecurityEventId(), LoginStatus.BLOCKED))
            return;
        var user = users.findById(challenge.getUserId()).orElseThrow();
        var history = new LoginHistory();
        history.setUserId(user.getId());
        history.setAttemptedUsername(user.getUsername());
        history.setIpAddress(challenge.getIpAddress());
        history.setCity(challenge.getCity());
        history.setCountry(challenge.getCountry());
        history.setLatitude(challenge.getLatitude());
        history.setLongitude(challenge.getLongitude());
        history.setLoginTime(java.time.LocalDateTime.now());
        history.setStatus(LoginStatus.BLOCKED);
        history.setFailureReason(
                "Đăng nhập bất thường: không xác minh OTP thành công trong thời hạn (" + challenge.getStatus() + ")");
        history.setSecurityEventId(challenge.getSecurityEventId());
        histories.save(history);
    }

    private void audit(EmailChallenge challenge, String action) {
        if (challenge.getSecurityEventId() != null)
            events.findById(challenge.getSecurityEventId()).ifPresent(event -> {
                event.setActionTaken(action);
                events.save(event);
            });
    }

    private LoginResponseDto pending(EmailChallenge challenge) {
        var response = error(202, "EMAIL_VERIFY".equals(challenge.getPurpose())
                ? "Vui lòng nhập mã được gửi đến email để hoàn tất đăng ký."
                : "Phát hiện đăng nhập bất thường. Để xác minh danh tính vui lòng nhập mã được gửi vào email của bạn.");
        response.setError(null);
        response.setVerificationRequired(true);
        response.setChallengeId(challenge.getId());
        response.setPurpose(challenge.getPurpose());
        String email = challenge.getEmail();
        response.setMaskedEmail(email.substring(0, 1) + "***" + email.substring(email.indexOf('@')));
        response.setExpiresAt(challenge.getExpiresAt());
        response.setResendAvailableAt(challenge.getLastSentAt().plusSeconds(60));
        return response;
    }
}
