package com.eventhub.service;

import com.eventhub.entity.PasswordResetToken;
import com.eventhub.entity.User;
import com.eventhub.repository.PasswordResetTokenRepository;
import com.eventhub.repository.RefreshTokenRepository;
import com.eventhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {
    private final UserRepository users;
    private final PasswordResetTokenRepository resetTokens;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.frontend-base-url:http://localhost:5173}")
    private String frontendBaseUrl;
    @Value("${app.password-reset.expiration-minutes:15}")
    private long expirationMinutes;

    /** Always returns the same public response regardless of whether the email exists. */
    @Transactional
    public void requestReset(String email) {
        // Do not create unusable tokens when email delivery is disabled.
        if (!emailService.isEnabled()) {
            log.warn("Password reset requested while mail delivery is disabled");
            return;
        }

        String normalized = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        var match = users.findByEmailIgnoreCaseForUpdate(normalized);
        if (match.isEmpty()) return;

        User user = match.get();
        Instant now = Instant.now();
        resetTokens.deleteExpiredOrOldUsed(now.minusSeconds(86400));
        resetTokens.markUnusedTokensAsUsed(user.getId(), now);

        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .tokenHash(hash(rawToken))
                .expiresAt(now.plusSeconds(Math.max(1, expirationMinutes) * 60))
                .build();
        resetTokens.save(token);

        String base = frontendBaseUrl == null ? "http://localhost:5173" : frontendBaseUrl.replaceAll("/+$", "");
        String link = base + "/reset-password?token=" + rawToken;
        String body = "We received a request to reset your EventHub password.\n\n"
                + "Use this one-time link within " + Math.max(1, expirationMinutes) + " minutes:\n"
                + link + "\n\nIf you did not request this, you can ignore this email.";
        try {
            emailService.sendRequired(user.getEmail(), "Reset your EventHub password", body);
        } catch (RuntimeException ex) {
            // Roll back both the new token and revocation of older tokens if delivery failed.
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.warn("Password reset email delivery failed for userId={}", user.getId());
        }
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("This password reset link is invalid or expired. Request a new one.");
        }
        validatePassword(newPassword);
        Instant now = Instant.now();
        PasswordResetToken token = resetTokens.findByTokenHashForUpdate(hash(rawToken))
                .orElseThrow(() -> invalidToken());
        if (token.getUsedAt() != null || token.getExpiresAt() == null || !token.getExpiresAt().isAfter(now)) {
            throw invalidToken();
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        users.save(user);

        token.setUsedAt(now);
        resetTokens.save(token);
        // A password reset invalidates existing sessions on all devices.
        refreshTokens.deleteAllByUserId(user.getId());
    }

    private IllegalArgumentException invalidToken() {
        return new IllegalArgumentException("This password reset link is invalid or expired. Request a new one.");
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 72
                || !password.matches(".*[A-Z].*")
                || !password.matches(".*[a-z].*")
                || !password.matches(".*\\d.*")
                || !password.matches(".*[^A-Za-z0-9].*")) {
            throw new IllegalArgumentException("Password must be 8-72 characters and include uppercase, lowercase, number and special character");
        }
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(64);
            for (byte b : digest) result.append(String.format("%02x", b));
            return result.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to hash password reset token", ex);
        }
    }
}
