package com.phenikaa.security.impossibletravel.integration;

import com.fasterxml.jackson.databind.*;
import com.phenikaa.security.impossibletravel.entity.*;
import com.phenikaa.security.impossibletravel.repository.*;
import com.phenikaa.security.impossibletravel.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:email-otp;MODE=MySQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
    "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop", "spring.sql.init.mode=never", "app.email.migrate=false", "spring.jpa.show-sql=false",
    "app.otp.expiration-scan-enabled=false"
})
@AutoConfigureMockMvc
class EmailOtpIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UserRepository users;
    @Autowired UserSessionRepository sessions;
    @Autowired EmailChallengeRepository challenges;
    @Autowired SecurityEventRepository events;
    @Autowired LoginHistoryRepository histories;
    @Autowired EmailOtpService otp;
    @Autowired PasswordEncoder encoder;
    @Autowired AuthService auth;
    @MockBean OtpMailService mail;
    String sentCode;
    static final String PASSWORD = "Password@123";

    @BeforeEach void captureMail() {
        doAnswer(invocation -> { sentCode = invocation.getArgument(1); return null; })
            .when(mail).send(anyString(), anyString(), anyBoolean(), nullable(String.class));
    }
    record Reply(int status, JsonNode body) {}
    Reply postJson(String path, Map<String, ?> payload) throws Exception {
        var result = mvc.perform(post("/api/auth" + path).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(payload))).andReturn();
        return new Reply(result.getResponse().getStatus(), json.readTree(result.getResponse().getContentAsString()));
    }
    User user(boolean verified) {
        var user = new User();
        user.setUsername("otp_" + UUID.randomUUID().toString().substring(0, 12));
        user.setPasswordHash(encoder.encode(PASSWORD)); user.setEmail(user.getUsername() + "@example.test"); user.setEmailVerified(verified);
        return users.save(user);
    }
    Reply login(User user, String ip) throws Exception {
        return postJson("/login", Map.of("username", user.getUsername(), "password", PASSWORD, "ipAddress", ip));
    }
    String blocked(User user) throws Exception {
        assertEquals(200, login(user, "118.70.128.1").status());
        var response = login(user, "128.122.1.1");
        assertEquals(202, response.status(), response.body().toString());
        assertFalse(response.body().has("sessionToken"));
        assertTrue(response.body().path("verificationRequired").asBoolean());
        return response.body().path("challengeId").asText();
    }
    Reply verify(String id, String code) throws Exception { return postJson("/otp/verify", Map.of("challengeId", id, "code", code)); }
    void allowResend(String id) {
        var challenge = challenges.findById(id).orElseThrow(); challenge.setLastSentAt(Instant.now().minusSeconds(61)); challenges.save(challenge);
    }
    @Test void registrationRequiresValidEmailAndVerificationBeforeLogin() throws Exception {
        String name = "reg_" + UUID.randomUUID().toString().substring(0, 12);
        assertEquals(400, postJson("/register", Map.of("username", name, "password", PASSWORD)).status());
        assertEquals(400, postJson("/register", Map.of("username", name, "password", PASSWORD, "email", "invalid")).status());
        var registered = postJson("/register", Map.of("username", name, "password", PASSWORD, "email", "reg@example.test"));
        assertEquals(201, registered.status());
        var user = users.findByUsername(name).orElseThrow();
        assertFalse(user.isEmailVerified());
        assertEquals(0, sessions.findByUserIdAndIsActiveTrue(user.getId()).size());
        assertNotEquals(200, login(user, "118.70.128.1").status());
        var id = registered.body().path("verification").path("challengeId").asText();
        var response = verify(id, sentCode);
        assertEquals(200, response.status()); assertFalse(response.body().has("sessionToken"));
        assertTrue(users.findById(user.getId()).orElseThrow().isEmailVerified());
        assertEquals(200, login(user, "118.70.128.1").status());
    }
    @Test void blockedLoginDoesNotIssueSessionUntilVerifiedAndCannotReplay() throws Exception {
        var user = user(true); String id = blocked(user);
        assertEquals(1, sessions.findByUserIdAndIsActiveTrue(user.getId()).size());
        String code = sentCode;
        assertEquals(0, histories.findByUserIdOrderByLoginTimeDesc(user.getId()).stream().filter(h -> h.getStatus() == LoginStatus.BLOCKED).count());
        var response = verify(id, code);
        assertEquals(200, response.status()); assertTrue(response.body().has("sessionToken"));
        assertEquals("128.122.1.1", response.body().path("location").path("ipAddress").asText());
        assertEquals(2, sessions.findByUserIdAndIsActiveTrue(user.getId()).size());
        var challenge = challenges.findById(id).orElseThrow();
        assertEquals("VERIFIED", challenge.getStatus());
        assertEquals("OTP_VERIFIED", events.findById(challenge.getSecurityEventId()).orElseThrow().getActionTaken());
        assertEquals(400, verify(id, code).status());
        assertEquals(2, sessions.findByUserIdAndIsActiveTrue(user.getId()).size());
        assertFalse(histories.existsBySecurityEventIdAndStatus(challenge.getSecurityEventId(), LoginStatus.BLOCKED));
    }
    @Test void wrongCodesArePersistedAndFiveFailuresLockTheChallenge() throws Exception {
        var user = user(true); String id = blocked(user); String correct = sentCode;
        String wrong = correct.equals("000000") ? "111111" : "000000";
        for (int i = 0; i < 4; i++) assertEquals(400, verify(id, wrong).status());
        assertEquals(429, verify(id, wrong).status());
        assertEquals("LOCKED", challenges.findById(id).orElseThrow().getStatus());
        assertEquals(400, verify(id, correct).status());
        assertEquals(1, sessions.findByUserIdAndIsActiveTrue(user.getId()).size());
    }
    @Test void expiredCodeNeverCreatesSession() throws Exception {
        var user = user(true); String id = blocked(user);
        var challenge = challenges.findById(id).orElseThrow(); challenge.setExpiresAt(Instant.now().minusSeconds(1)); challenges.save(challenge);
        assertEquals(400, verify(id, sentCode).status());
        assertEquals("EXPIRED", challenges.findById(id).orElseThrow().getStatus());
        assertEquals(1, sessions.findByUserIdAndIsActiveTrue(user.getId()).size());
    }
    @Test void resendThrottlesInvalidatesOldCodeAndDoesNotResetAttempts() throws Exception {
        var user = user(true); String id = blocked(user); String oldCode = sentCode;
        assertEquals(429, postJson("/otp/resend", Map.of("challengeId", id)).status());
        allowResend(id);
        assertEquals(202, postJson("/otp/resend", Map.of("challengeId", id)).status());
        assertNotEquals(oldCode, sentCode);
        String newCode = sentCode;
        assertEquals(400, verify(id, oldCode).status());
        assertEquals(1, challenges.findById(id).orElseThrow().getAttempts());
        assertEquals(200, verify(id, newCode).status());
    }
    @Test void cancellationAndUnknownChallengeCannotAuthenticate() throws Exception {
        var user = user(true); String id = blocked(user);
        assertEquals(200, postJson("/otp/cancel", Map.of("challengeId", id)).status());
        assertEquals(400, verify(id, sentCode).status());
        assertEquals(400, verify(UUID.randomUUID().toString(), "123456").status());
        assertEquals(1, sessions.findByUserIdAndIsActiveTrue(user.getId()).size());
    }
    @Test void smtpFailureFailsClosedAndPreservesOldCodeOnFailedResend() throws Exception {
        var user = user(true); String id = blocked(user); String oldCode = sentCode;
        allowResend(id);
        doThrow(new IllegalStateException("SMTP unavailable")).when(mail).send(anyString(), anyString(), anyBoolean(), nullable(String.class));
        assertEquals(503, postJson("/otp/resend", Map.of("challengeId", id)).status());
        assertEquals(1, challenges.findById(id).orElseThrow().getSendCount());
        assertEquals(200, verify(id, oldCode).status());
        var another = user(true);
        assertEquals(200, login(another, "118.70.128.1").status());
        assertEquals(503, login(another, "128.122.1.1").status());
        assertEquals(1, sessions.findByUserIdAndIsActiveTrue(another.getId()).size());
        assertTrue(challenges.findByUserIdAndStatus(another.getId(), "PENDING").isEmpty());
    }
    @Test void legacyAccountWithoutEmailRemainsBlocked() throws Exception {
        var user = user(false); user.setEmail(null); users.save(user);
        assertEquals(200, login(user, "118.70.128.1").status());
        assertEquals(403, login(user, "128.122.1.1").status());
        verifyNoInteractions(mail);
    }
    @Test void wrongPasswordNeverSendsOtp() throws Exception {
        var user = user(true);
        assertEquals(401, postJson("/login", Map.of("username", user.getUsername(), "password", "wrong", "ipAddress", "128.122.1.1")).status());
        verifyNoInteractions(mail);
    }
    @Test void repeatedLoginCannotBypassSendCooldownAndNewChallengeSupersedesOld() throws Exception {
        var user = user(true); String id = blocked(user); String oldCode = sentCode;
        assertEquals(429, login(user, "128.122.1.1").status());
        allowResend(id);
        var next = login(user, "128.122.1.1"); assertEquals(202, next.status());
        assertEquals("SUPERSEDED", challenges.findById(id).orElseThrow().getStatus());
        assertEquals(400, verify(id, oldCode).status());
        assertEquals(200, verify(next.body().path("challengeId").asText(), sentCode).status());
    }
    @Test void twoConcurrentVerificationsCanIssueOnlyOneSession() throws Exception {
        var user = user(true); String id = blocked(user); String code = sentCode;
        try (var pool = Executors.newFixedThreadPool(2)) {
            var first = pool.submit(() -> auth.verifyEmailCode(id, code));
            var second = pool.submit(() -> auth.verifyEmailCode(id, code));
            int successes = (first.get(15, TimeUnit.SECONDS).isSuccess() ? 1 : 0) + (second.get(15, TimeUnit.SECONDS).isSuccess() ? 1 : 0);
            assertEquals(1, successes);
        }
        assertEquals(2, sessions.findByUserIdAndIsActiveTrue(user.getId()).size());
    }
    @Test void onlyThreeEmailsAllowedPerChallenge() throws Exception {
        var user = user(true); String id = blocked(user);
        for (int i = 0; i < 2; i++) {
            allowResend(id); assertEquals(202, postJson("/otp/resend", Map.of("challengeId", id)).status());
        }
        allowResend(id); assertEquals(429, postJson("/otp/resend", Map.of("challengeId", id)).status());
        assertEquals(3, challenges.findById(id).orElseThrow().getSendCount());
    }

    void expire(String id) {
        var challenge = challenges.findById(id).orElseThrow();
        challenge.setExpiresAt(Instant.now().minusSeconds(1)); challenges.save(challenge);
    }
    @Test void backgroundScanRecordsTimeoutWithoutAnyClientRequestAndOnlyOnce() throws Exception {
        var user = user(true); String id = blocked(user);
        var challenge = challenges.findById(id).orElseThrow();
        var task = new OtpExpirationTask(challenges, otp);
        task.scan();
        assertFalse(histories.existsBySecurityEventIdAndStatus(challenge.getSecurityEventId(), LoginStatus.BLOCKED));
        expire(id);
        task.scan(); task.scan(); otp.finalizeExpiredLogin(id);
        assertEquals("EXPIRED", challenges.findById(id).orElseThrow().getStatus());
        assertEquals("OTP_EXPIRED", events.findById(challenge.getSecurityEventId()).orElseThrow().getActionTaken());
        assertEquals(1, histories.findByUserIdOrderByLoginTimeDesc(user.getId()).stream().filter(h -> h.getStatus() == LoginStatus.BLOCKED).count());
        assertEquals(1, sessions.findByUserIdAndIsActiveTrue(user.getId()).size());
        assertTrue(histories.findByUserIdOrderByLoginTimeDesc(user.getId()).stream().anyMatch(h -> h.getStatus() == LoginStatus.BLOCKED && h.getFailureReason().contains("trong thời hạn")));
    }
    @Test void verifiedAttemptNeverBecomesBlockedAfterItsOriginalDeadline() throws Exception {
        var user = user(true); String id = blocked(user);
        assertEquals(200, verify(id, sentCode).status());
        expire(id);
        new OtpExpirationTask(challenges, otp).scan(); otp.finalizeExpiredLogin(id);
        assertEquals("VERIFIED", challenges.findById(id).orElseThrow().getStatus());
        assertEquals(0, histories.findByUserIdOrderByLoginTimeDesc(user.getId()).stream().filter(h -> h.getStatus() == LoginStatus.BLOCKED).count());
    }
    @Test void cancellingEarlyDefersFailureHistoryUntilDeadline() throws Exception {
        var user = user(true); String id = blocked(user);
        assertEquals(200, postJson("/otp/cancel", Map.of("challengeId", id)).status());
        assertEquals(0, histories.findByUserIdOrderByLoginTimeDesc(user.getId()).stream().filter(h -> h.getStatus() == LoginStatus.BLOCKED).count());
        expire(id);
        new OtpExpirationTask(challenges, otp).scan();
        assertEquals(1, histories.findByUserIdOrderByLoginTimeDesc(user.getId()).stream().filter(h -> h.getStatus() == LoginStatus.BLOCKED).count());
        assertEquals("CANCELLED", challenges.findById(id).orElseThrow().getStatus());
    }
}
