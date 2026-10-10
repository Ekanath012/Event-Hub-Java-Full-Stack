package com.eventhub.dto;

import com.eventhub.entity.Role;
import com.eventhub.entity.User;
import java.time.Instant;

public record UserResponse(Long id, String firstName, String lastName, String email, String phone,
                           String profileImage, Role role, boolean enabled, Instant createdAt, Instant lastLoginAt) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getFirstName(), u.getLastName(), u.getEmail(), u.getPhone(),
                u.getProfileImage(), u.getRole(), u.isEnabled(), u.getCreatedAt(), u.getLastLoginAt());
    }
}
