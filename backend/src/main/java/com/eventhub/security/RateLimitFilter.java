package com.eventhub.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final int maxRequests;
    private final long windowSeconds;

    public RateLimitFilter(@Value("${app.rate-limit.auth.max-requests:10}") int maxRequests,
                           @Value("${app.rate-limit.auth.window-seconds:60}") long windowSeconds) {
        this.maxRequests = Math.max(1, maxRequests);
        this.windowSeconds = Math.max(1, windowSeconds);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/auth/")
                || !("POST".equalsIgnoreCase(request.getMethod()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String key = request.getRemoteAddr() + ":" + request.getRequestURI();
        long now = Instant.now().getEpochSecond();
        Window current = windows.compute(key, (k, old) -> {
            if (old == null || now - old.startedAt >= windowSeconds) return new Window(now, new AtomicInteger(1));
            old.count.incrementAndGet();
            return old;
        });
        if (current.count.get() > maxRequests) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", String.valueOf(windowSeconds));
            response.getWriter().write("{\"status\":429,\"message\":\"Too many authentication requests. Try again later.\"}");
            return;
        }
        chain.doFilter(request, response);
    }

    private record Window(long startedAt, AtomicInteger count) {}
}
