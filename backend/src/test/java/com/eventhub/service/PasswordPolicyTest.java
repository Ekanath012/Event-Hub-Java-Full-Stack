package com.eventhub.service;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class PasswordPolicyTest {
    @Test
    void passwordPolicyRejectsWeakPasswords() throws Exception {
        AuthService service = new AuthService(null, null, null, null, null);
        Method method = AuthService.class.getDeclaredMethod("validatePassword", String.class);
        method.setAccessible(true);
        assertThrows(Exception.class, () -> method.invoke(service, "password"));
        assertThrows(Exception.class, () -> method.invoke(service, "Password1"));
    }
}
