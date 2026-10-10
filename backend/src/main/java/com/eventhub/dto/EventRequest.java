package com.eventhub.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;

public record EventRequest(
        @NotBlank @Size(max = 180) String title,
        @NotBlank @Size(max = 10000) String description,
        @Size(max = 500) String bannerImage,
        @NotNull Long categoryId,
        @NotNull Long venueId,
        @NotNull @FutureOrPresent LocalDate eventDate,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        @NotNull LocalDate registrationStart,
        @NotNull LocalDate registrationDeadline,
        @NotNull @Min(1) @Max(1000000) Integer maxAttendees,
        @Size(max = 10000) String rules,
        @Email @Size(max = 190) String contactEmail,
        @Size(max = 30) String contactPhone
) {}
