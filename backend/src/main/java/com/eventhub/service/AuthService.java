package com.eventhub.service;

import com.eventhub.dto.*;
import com.eventhub.entity.RefreshToken;
import com.eventhub.entity.User;
import com.eventhub.repository.RefreshTokenRepository;
import com.eventhub.repository.UserRepository;
import com.eventhub.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditLogService audit;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.jwt.refresh-token-expiration-seconds:604800}")
    private long refreshExpirationSeconds;
    @Value("${app.security.max-failed-logins:5}")
    private int maxFailedLogins;
    @Value("${app.security.lockout-minutes:15}")
    private long lockoutMinutes;

    @Transactional
    public User register(RegisterRequest request, String ip) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new IllegalArgumentException("Email is already registered");
        }
        validatePassword(request.password());
        User user = User.builder()
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .email(request.email().trim().toLowerCase())
                .phone(request.phone() == null ? null : request.phone().trim())
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();
        User saved = userRepository.save(user);
        audit.log(saved, "USER_CREATED", "USER", saved.getId(), ip, "User registered");
        return saved;
    }

    @Transactional(noRollbackFor = BadCredentialsException.class)
    public AuthSession login(LoginRequest request, String ip) {
        User user = userRepository.findByEmailIgnoreCaseForUpdate(request.email().trim()).orElse(null);
        if (user == null) {
            audit.log(null, "FAILED_LOGIN", "AUTH", null, ip, "Failed login attempt");
            throw new BadCredentialsException("Invalid email or password");
        }
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            audit.log(user, "FAILED_LOGIN", "AUTH", user.getId(), ip, "Login rejected for locked account");
            throw new BadCredentialsException("Account temporarily locked. Try again later");
        }
        if (user.isLocked() || !user.isEnabled()) {
            audit.log(user, "FAILED_LOGIN", "AUTH", user.getId(), ip, "Login rejected for disabled/locked account");
            throw new BadCredentialsException("Invalid email or password");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= maxFailedLogins) {
                user.setLockedUntil(Instant.now().plusSeconds(lockoutMinutes * 60));
                audit.log(user, "ACCOUNT_LOCKED", "USER", user.getId(), ip, "Too many failed login attempts");
            }
            userRepository.save(user);
            audit.log(user, "FAILED_LOGIN", "AUTH", user.getId(), ip, "Invalid password");
            throw new BadCredentialsException("Invalid email or password");
        }

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
        audit.log(user, "LOGIN", "AUTH", user.getId(), ip, "Login successful");
        // Keep other devices signed in. Remove only expired or long-revoked records as housekeeping.
        Instant now = Instant.now();
        refreshTokenRepository.deleteExpiredForUser(user.getId(), now);
        refreshTokenRepository.deleteOldRevokedForUser(user.getId(), now.minusSeconds(86400));
        String refreshRaw = createRefreshToken(user);
        return new AuthSession(jwtService.generateAccessToken(user), refreshRaw, UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public UserResponse me(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadCredentialsException("User not found"));
        if (!user.isEnabled() || user.isLocked() || (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now()))) {
            throw new BadCredentialsException("Account is disabled or locked");
        }
        return UserResponse.from(user);
    }

    @Transactional(noRollbackFor = BadCredentialsException.class)
    public AuthSession refresh(String rawRefreshToken, String ip) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            log.warn("Refresh rejected: missing_cookie ip={}", ip);
            throw new BadCredentialsException("Refresh token is required");
        }
        var match = refreshTokenRepository.findByTokenHashForUpdate(hash(rawRefreshToken));
        if (match.isEmpty()) {
            log.warn("Refresh rejected: token_not_found ip={}", ip);
            throw new BadCredentialsException("Invalid refresh token");
        }
        RefreshToken stored = match.get();
        Instant now = Instant.now();
        if (stored.isRevoked() || !stored.getExpiresAt().isAfter(now)) {
            String reason = stored.isRevoked() ? "token_revoked" : "token_expired";
            log.warn("Refresh rejected: {} userId={} ip={}", reason, stored.getUser().getId(), ip);
            stored.setRevoked(true);
            refreshTokenRepository.save(stored);
            audit.log(stored.getUser(), "REFRESH_REJECTED", "AUTH", stored.getUser().getId(), ip, reason);
            throw new BadCredentialsException("Refresh token expired or revoked");
        }
        User user = stored.getUser();
        if (!user.isEnabled() || user.isLocked() || (user.getLockedUntil() != null && user.getLockedUntil().isAfter(now))) {
            log.warn("Refresh rejected: account_unavailable userId={} ip={}", user.getId(), ip);
            throw new BadCredentialsException("Account is disabled or locked");
        }
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);
        String newRaw = createRefreshToken(user);
        audit.log(user, "TOKEN_REFRESH", "AUTH", user.getId(), ip, "Refresh token rotated");
        return new AuthSession(jwtService.generateAccessToken(user), newRaw, UserResponse.from(user));
    }

    @Transactional
    public void logout(String rawRefreshToken, String ip) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) return;
        refreshTokenRepository.findByTokenHashForUpdate(hash(rawRefreshToken)).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            audit.log(token.getUser(), "LOGOUT", "AUTH", token.getUser().getId(), ip, "Logout successful");
        });
    }

    private void validatePassword(String password) {
        if (password.length() < 8 || password.length() > 72
                || !password.matches(".*[A-Z].*")
                || !password.matches(".*[a-z].*")
                || !password.matches(".*\\d.*")
                || !password.matches(".*[^A-Za-z0-9].*")) {
            throw new IllegalArgumentException("Password must be 8-72 characters and include uppercase, lowercase, number and special character");
        }
    }

    private String createRefreshToken(User user) {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        RefreshToken token = RefreshToken.builder().user(user).tokenHash(hash(raw))
                .expiresAt(Instant.now().plusSeconds(refreshExpirationSeconds)).build();
        refreshTokenRepository.save(token);
        return raw;
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to hash token", ex);
        }
    }

    public record AuthSession(String accessToken, String refreshToken, UserResponse user) {}
}
