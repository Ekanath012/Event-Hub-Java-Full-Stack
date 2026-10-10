package com.eventhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VenueRequest(
        @NotBlank @Size(max = 160) String name,
        @NotBlank @Size(max = 300) String address,
        @NotBlank @Size(max = 100) String city,
        @Size(max = 100) String state,
        @Size(max = 20) String postalCode,
        Boolean active
) {}
