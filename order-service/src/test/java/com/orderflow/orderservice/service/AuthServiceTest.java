package com.orderflow.orderservice.service;

import com.orderflow.common.exception.BadRequestException;
import com.orderflow.common.security.JwtUtil;
import com.orderflow.orderservice.dto.AuthResponse;
import com.orderflow.orderservice.dto.LoginRequest;
import com.orderflow.orderservice.dto.RegisterRequest;
import com.orderflow.orderservice.entity.Role;
import com.orderflow.orderservice.entity.User;
import com.orderflow.orderservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@orderflow.com")
                .password("encoded_password")
                .firstName("Test")
                .lastName("User")
                .role(Role.USER)
                .build();
    }

    @Test
    void register_Success() {
        RegisterRequest request = new RegisterRequest("test@orderflow.com", "password123", "Test", "User");

        when(userRepository.existsByEmail("test@orderflow.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken(eq("test@orderflow.com"), eq("USER"), eq(1L))).thenReturn("jwt_token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("jwt_token", response.getToken());
        assertEquals("test@orderflow.com", response.getUser().getEmail());
    }

    @Test
    void register_DuplicateEmail_ThrowsException() {
        RegisterRequest request = new RegisterRequest("test@orderflow.com", "password123", "Test", "User");
        when(userRepository.existsByEmail("test@orderflow.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(request));
    }

    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest("test@orderflow.com", "password123");

        when(userRepository.findByEmail("test@orderflow.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);
        when(jwtUtil.generateToken(eq("test@orderflow.com"), eq("USER"), eq(1L))).thenReturn("jwt_token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt_token", response.getToken());
    }

    @Test
    void login_InvalidPassword_ThrowsException() {
        LoginRequest request = new LoginRequest("test@orderflow.com", "wrong");

        when(userRepository.findByEmail("test@orderflow.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrong", "encoded_password")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> authService.login(request));
    }

    @Test
    void login_UserNotFound_ThrowsException() {
        LoginRequest request = new LoginRequest("unknown@orderflow.com", "password");

        when(userRepository.findByEmail("unknown@orderflow.com")).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> authService.login(request));
    }
}
