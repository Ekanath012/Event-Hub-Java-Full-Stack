package com.eventhub.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @NotBlank @Email @Size(max = 190) String email,
        @Size(max = 30) String phone,
        @NotBlank @Size(min = 8, max = 72) String password
) {}
