package com.eventhub.security;

import com.eventhub.entity.User;
import com.eventhub.entity.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    @Test
    void generatedTokenCanBeValidatedForSameUser() {
        JwtService service = new JwtService("12345678901234567890123456789012", 900);
        User user = User.builder().id(7L).email("user@example.com").role(Role.STUDENT).enabled(true).locked(false).build();
        String token = service.generateAccessToken(user);
        assertEquals("user@example.com", service.extractSubject(token));
        assertTrue(service.isValid(token, user));
    }

    @Test
    void weakSecretIsRejected() {
        assertThrows(IllegalStateException.class, () -> new JwtService("short-secret", 900));
    }
}
