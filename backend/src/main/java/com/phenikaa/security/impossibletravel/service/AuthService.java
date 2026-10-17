package com.phenikaa.security.impossibletravel.service;

import com.phenikaa.security.impossibletravel.dto.RegisterRequestDto;
import com.phenikaa.security.impossibletravel.dto.RegisterResponseDto;
import com.phenikaa.security.impossibletravel.entity.Role;
import com.phenikaa.security.impossibletravel.entity.User;
import com.phenikaa.security.impossibletravel.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Đăng ký tài khoản người dùng mới với mật khẩu băm BCrypt
     */
    @Transactional
    public RegisterResponseDto register(RegisterRequestDto request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return RegisterResponseDto.error("Tên đăng nhập không được để trống");
        }
        if (request.getPassword() == null || request.getPassword().trim().length() < 6) {
            return RegisterResponseDto.error("Mật khẩu phải có độ dài tối thiểu 6 ký tự");
        }

        String username = request.getUsername().trim();
        if (userRepository.existsByUsername(username)) {
            return RegisterResponseDto.error("Tên đăng nhập đã tồn tại trong hệ thống");
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName() != null ? request.getFullName().trim() : username);
        user.setRole(Role.ROLE_USER);
        user.setIsActive(true);

        User savedUser = userRepository.save(user);
        log.info("Đăng ký thành công người dùng mới: id={}, username={}", savedUser.getId(), savedUser.getUsername());

        return RegisterResponseDto.success(savedUser.getId(), savedUser.getUsername());
    }
}
