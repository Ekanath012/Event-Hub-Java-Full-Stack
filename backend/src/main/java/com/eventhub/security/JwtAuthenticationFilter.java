package com.eventhub.security;

import com.eventhub.entity.User;
import com.eventhub.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                String email = jwtService.extractSubject(token);
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    userRepository.findByEmailIgnoreCase(email).ifPresent(user -> authenticateIfValid(token, user));
                }
            } catch (RuntimeException ignored) {
                // Invalid token: leave the request unauthenticated; authorization will reject protected endpoints.
            }
        }
        chain.doFilter(request, response);
    }

    private void authenticateIfValid(String token, User user) {
        if (!jwtService.isValid(token, user)) return;
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        var authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
