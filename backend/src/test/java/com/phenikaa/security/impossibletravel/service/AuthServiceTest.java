package com.phenikaa.security.impossibletravel.service;

import com.phenikaa.security.impossibletravel.dto.RegisterRequestDto;
import com.phenikaa.security.impossibletravel.dto.RegisterResponseDto;
import com.phenikaa.security.impossibletravel.entity.User;
import com.phenikaa.security.impossibletravel.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử AuthService - Chức năng Đăng ký người dùng")
class AuthServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Đăng ký thành công người dùng mới với mật khẩu được băm BCrypt")
    void testRegisterSuccess() {
        RegisterRequestDto request = new RegisterRequestDto("student01", "Password@123", "Nguyen Van A");

        when(userRepository.existsByUsername("student01")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        RegisterResponseDto response = authService.register(request);

        assertTrue(response.isSuccess());
        assertEquals(1L, response.getUserId());
        assertEquals("student01", response.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Từ chối khi tên đăng nhập đã tồn tại")
    void testRegisterDuplicateUsername() {
        RegisterRequestDto request = new RegisterRequestDto("student01", "Password@123", "Nguyen Van A");
        when(userRepository.existsByUsername("student01")).thenReturn(true);

        RegisterResponseDto response = authService.register(request);

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("đã tồn tại"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Từ chối khi mật khẩu quá ngắn (< 6 ký tự)")
    void testRegisterShortPassword() {
        RegisterRequestDto request = new RegisterRequestDto("student01", "123", "Nguyen Van A");

        RegisterResponseDto response = authService.register(request);

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("tối thiểu 6 ký tự"));
    }
}
