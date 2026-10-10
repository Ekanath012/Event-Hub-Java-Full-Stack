package com.eventhub.dto; import jakarta.validation.constraints.NotBlank; public record CheckInRequest(@NotBlank String qrPayload){}
