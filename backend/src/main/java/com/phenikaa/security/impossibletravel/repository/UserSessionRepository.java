package com.phenikaa.security.impossibletravel.repository;

import com.phenikaa.security.impossibletravel.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    List<UserSession> findByUserIdAndIsActiveTrue(Long userId);

    Optional<UserSession> findBySessionToken(String sessionToken);

    Optional<UserSession> findBySessionTokenAndIsActiveTrue(String sessionToken);

    List<UserSession> findByIsActiveTrue();

    long countByIsActiveTrue();
}
