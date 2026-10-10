package com.eventhub.dto;

import com.eventhub.entity.EventStatus;
import jakarta.validation.constraints.NotNull;

public record EventStatusRequest(@NotNull EventStatus status) {}
