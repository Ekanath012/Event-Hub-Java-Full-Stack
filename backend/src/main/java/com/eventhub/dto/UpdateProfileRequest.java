package com.eventhub.dto;

import jakarta.validation.constraints.*;

public record UpdateProfileRequest(
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @Size(max = 30) String phone,
        @Size(max = 500) String profileImage
) {}
