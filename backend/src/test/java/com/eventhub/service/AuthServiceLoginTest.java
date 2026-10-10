package com.eventhub.service;

import com.eventhub.dto.LoginRequest;
import com.eventhub.dto.UserResponse;
import com.eventhub.entity.Role;
import com.eventhub.entity.User;
import com.eventhub.repository.RefreshTokenRepository;
import com.eventhub.repository.UserRepository;
import com.eventhub.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceLoginTest {
    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuditLogService auditLogService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, refreshTokenRepository,
                passwordEncoder, jwtService, auditLogService);
        ReflectionTestUtils.setField(authService, "refreshExpirationSeconds", 604800L);
    }

    @Test
    void studentLoginContinuesWhenCleanupFindsNoTokens() {
        assertLoginWorksForRole(Role.STUDENT, "student@example.com");
    }

    @Test
    void adminLoginContinuesWhenCleanupFindsNoTokens() {
        assertLoginWorksForRole(Role.ADMIN, "admin@example.com");
    }

    private void assertLoginWorksForRole(Role role, String email) {
        User user = User.builder()
                .id(42L)
                .firstName("Test")
                .lastName("User")
                .email(email)
                .passwordHash("stored-hash")
                .role(role)
                .enabled(true)
                .locked(false)
                .failedLoginAttempts(0)
                .build();

        when(userRepository.findByEmailIgnoreCaseForUpdate(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("StrongPassword1!", "stored-hash")).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("test-access-token");

        AuthService.AuthSession session = authService.login(
                new LoginRequest(email, "StrongPassword1!"), "127.0.0.1");

        assertNotNull(session);
        assertEquals("test-access-token", session.accessToken());
        assertNotNull(session.refreshToken());
        UserResponse response = session.user();
        assertEquals(role, response.role());

        verify(refreshTokenRepository).deleteExpiredForUser(eq(user.getId()), any());
        verify(refreshTokenRepository).deleteOldRevokedForUser(eq(user.getId()), any());
        verify(refreshTokenRepository).save(any(com.eventhub.entity.RefreshToken.class));
    }
}
