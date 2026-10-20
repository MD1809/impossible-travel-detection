package com.phenikaa.security.impossibletravel.repository;

import com.phenikaa.security.impossibletravel.entity.EmailChallenge;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.*;

public interface EmailChallengeRepository extends JpaRepository<EmailChallenge, String> {
    @Query("select c.userId from EmailChallenge c where c.id = :id")
    Optional<Long> findUserIdByChallengeId(@Param("id") String id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from EmailChallenge c where c.id = :id")
    Optional<EmailChallenge> lockById(@Param("id") String id);

    List<EmailChallenge> findByUserIdAndStatus(Long userId, String status);

    Optional<EmailChallenge> findFirstByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndCreatedAtAfter(Long userId, Instant after);

    @Query("select c.id from EmailChallenge c where c.purpose = 'LOGIN' and c.status <> 'VERIFIED' "
            + "and c.expiresAt <= :now and c.securityEventId is not null "
            + "and not exists (select h.id from LoginHistory h where h.securityEventId = c.securityEventId "
            + "and h.status = com.phenikaa.security.impossibletravel.entity.LoginStatus.BLOCKED) order by c.expiresAt, c.id")
    List<String> findUnrecordedExpiredLogins(@Param("now") Instant now,
            org.springframework.data.domain.Pageable pageable);
}
