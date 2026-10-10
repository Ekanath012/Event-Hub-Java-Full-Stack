package com.eventhub.service;

import com.eventhub.entity.PasswordResetToken;
import com.eventhub.entity.Role;
import com.eventhub.entity.User;
import com.eventhub.repository.PasswordResetTokenRepository;
import com.eventhub.repository.RefreshTokenRepository;
import com.eventhub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {
    @Mock private UserRepository users;
    @Mock private PasswordResetTokenRepository resetTokens;
    @Mock private RefreshTokenRepository refreshTokens;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;

    private PasswordResetService service;

    @BeforeEach
    void setUp() {
        service = new PasswordResetService(users, resetTokens, refreshTokens, passwordEncoder, emailService);
        ReflectionTestUtils.setField(service, "frontendBaseUrl", "http://localhost:5173");
        ReflectionTestUtils.setField(service, "expirationMinutes", 15L);
    }

    @Test
    void requestResetSendsSingleUseLinkAndStoresOnlyTokenHash() {
        User user = User.builder().id(7L).email("student@example.com").firstName("Student")
                .lastName("One").passwordHash("old-hash").role(Role.STUDENT).enabled(true).build();
        when(emailService.isEnabled()).thenReturn(true);
        when(users.findByEmailIgnoreCase("student@example.com")).thenReturn(Optional.of(user));
        when(resetTokens.save(any(PasswordResetToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.requestReset(" student@example.com ");

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(resetTokens).save(tokenCaptor.capture());
        PasswordResetToken stored = tokenCaptor.getValue();
        assertEquals(64, stored.getTokenHash().length());
        assertNotNull(stored.getExpiresAt());
        assertNull(stored.getUsedAt());

        ArgumentCaptor<String> emailBody = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendRequired(eq("student@example.com"), eq("Reset your EventHub password"), emailBody.capture());
        assertTrue(emailBody.getValue().contains("http://localhost:5173/reset-password?token="));
        assertFalse(emailBody.getValue().contains(stored.getTokenHash()));
    }

    @Test
    void requestResetDoesNotRevealOrSendWhenEmailIsUnknown() {
        when(emailService.isEnabled()).thenReturn(true);
        when(users.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> service.requestReset("missing@example.com"));

        verify(resetTokens, never()).save(any());
        verify(emailService, never()).sendRequired(anyString(), anyString(), anyString());
    }

    @Test
    void resetPasswordChangesHashConsumesTokenAndInvalidatesSessions() throws Exception {
        String rawToken = "example-one-time-reset-token";
        User user = User.builder().id(7L).email("student@example.com").firstName("Student")
                .lastName("One").passwordHash("old-hash").role(Role.STUDENT).enabled(true).locked(false).build();
        PasswordResetToken token = PasswordResetToken.builder().id(3L).user(user)
                .tokenHash(hash(rawToken)).expiresAt(Instant.now().plusSeconds(600)).build();
        when(resetTokens.findByTokenHashForUpdate(hash(rawToken))).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("StrongPass1!")).thenReturn("new-encoded-hash");
        when(users.save(user)).thenReturn(user);

        service.resetPassword(rawToken, "StrongPass1!");

        assertEquals("new-encoded-hash", user.getPasswordHash());
        assertNotNull(token.getUsedAt());
        verify(resetTokens).save(token);
        verify(refreshTokens).deleteAllByUserId(7L);
        verify(users).save(user);
    }

    @Test
    void resetPasswordRejectsExpiredOrUnknownTokens() {
        when(resetTokens.findByTokenHashForUpdate(anyString())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.resetPassword("bad-token", "StrongPass1!"));
        verify(users, never()).save(any());
    }

    private String hash(String value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8)));
    }
}
