package com.phenikaa.security.impossibletravel.service;

import com.phenikaa.security.impossibletravel.repository.EmailChallengeRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.Instant;

@Component
@EnableScheduling
@ConditionalOnProperty(name = "app.otp.expiration-scan-enabled", havingValue = "true", matchIfMissing = true)
public class OtpExpirationTask {
    private final EmailChallengeRepository challenges;
    private final EmailOtpService otp;
    public OtpExpirationTask(EmailChallengeRepository challenges, EmailOtpService otp) {
        this.challenges = challenges; this.otp = otp;
    }
    @Scheduled(fixedDelayString = "${app.otp.expiration-scan-ms:5000}")
    public void scan() {
        for (String id : challenges.findUnrecordedExpiredLogins(Instant.now(), PageRequest.of(0, 100))) {
            otp.finalizeExpiredLogin(id);
        }
    }
}
