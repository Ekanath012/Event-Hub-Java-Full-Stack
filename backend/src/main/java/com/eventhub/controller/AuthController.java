package com.eventhub.controller;

import com.eventhub.dto.*;
import com.eventhub.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final String REFRESH_COOKIE = "eventhub_refresh";
    private final AuthService authService;

    @Value("${app.cookie.secure:false}") private boolean secureCookie;
    @Value("${app.jwt.access-token-expiration-seconds:900}") private long accessExpirationSeconds;
    @Value("${app.jwt.refresh-token-expiration-seconds:604800}") private long refreshExpirationSeconds;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest http) {
        return ResponseEntity.ok(UserResponse.from(authService.register(request, http.getRemoteAddr())));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http, HttpServletResponse response) {
        var session = authService.login(request, http.getRemoteAddr());
        setRefreshCookie(response, session.refreshToken(), refreshExpirationSeconds);
        return ResponseEntity.ok(new AuthResponse(session.accessToken(), accessExpirationSeconds, session.user()));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        return ResponseEntity.ok(authService.me(authentication.getName()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@CookieValue(value = REFRESH_COOKIE, required = false) String refresh, HttpServletRequest http, HttpServletResponse response) {
        var session = authService.refresh(refresh, http.getRemoteAddr());
        setRefreshCookie(response, session.refreshToken(), refreshExpirationSeconds);
        return ResponseEntity.ok(new AuthResponse(session.accessToken(), accessExpirationSeconds, session.user()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(value = REFRESH_COOKIE, required = false) String refresh, HttpServletRequest http, HttpServletResponse response) {
        authService.logout(refresh, http.getRemoteAddr());
        clearRefreshCookie(response);
        return ResponseEntity.noContent().build();
    }

    private void setRefreshCookie(HttpServletResponse response, String token, long maxAge) {
        response.addHeader("Set-Cookie", ResponseCookie.from(REFRESH_COOKIE, token)
                .httpOnly(true).secure(secureCookie).sameSite("Lax").path("/api/auth")
                .maxAge(maxAge).build().toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        response.addHeader("Set-Cookie", ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true).secure(secureCookie).sameSite("Lax").path("/api/auth")
                .maxAge(0).build().toString());
    }
}
