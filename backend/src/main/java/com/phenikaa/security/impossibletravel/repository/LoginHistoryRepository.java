package com.phenikaa.security.impossibletravel.repository;

import com.phenikaa.security.impossibletravel.entity.LoginHistory;
import com.phenikaa.security.impossibletravel.entity.LoginStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    // Kịch bản 2: Tìm mốc đăng nhập SUCCESS gần nhất để đối chiếu
    Optional<LoginHistory> findFirstByUserIdAndStatusOrderByLoginTimeDesc(Long userId, LoginStatus status);

    List<LoginHistory> findByUserIdOrderByLoginTimeDesc(Long userId);

    Page<LoginHistory> findAllByOrderByLoginTimeDesc(Pageable pageable);

    long countByStatus(LoginStatus status);
}
