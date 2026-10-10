package com.eventhub.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender sender;

    @Value("${app.mail.enabled:false}")
    private boolean enabled;

    @Value("${spring.mail.username:}")
    private String from;

    public boolean isEnabled() {
        return enabled;
    }

    /** Strict delivery for security-critical messages such as password reset. */
    public void sendRequired(String to, String subject, String body) {
        if (!enabled) throw new IllegalStateException("Password reset email delivery is disabled");
        if (to == null || to.isBlank()) throw new IllegalArgumentException("Email recipient is required");

        SimpleMailMessage message = new SimpleMailMessage();
        if (from != null && !from.isBlank()) message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        sender.send(message);
    }

    public void send(String to, String subject, String body) {
        if (!enabled || to == null || to.isBlank()) return;

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (from != null && !from.isBlank()) {
                message.setFrom(from);
            }
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            sender.send(message);
        } catch (RuntimeException ex) {
            // Email is a side effect; a temporary mail-provider failure must not
            // roll back a successful registration, cancellation, or waitlist promotion.
            log.warn("EventHub email delivery failed for recipient {}: {}", to, ex.getMessage());
        }
    }
}
