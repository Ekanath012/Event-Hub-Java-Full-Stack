package com.eventhub.dto;

public record AuthResponse(String accessToken, long expiresInSeconds, UserResponse user) {}
