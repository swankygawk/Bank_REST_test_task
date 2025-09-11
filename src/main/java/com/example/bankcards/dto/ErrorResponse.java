package com.example.bankcards.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
    int statusCode,
    String message,
    String description,
    LocalDateTime timestamp
) {}
