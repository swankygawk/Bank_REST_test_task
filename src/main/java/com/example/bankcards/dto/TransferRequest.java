package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransferRequest(
    @NotBlank(message = "Source card ID cannot be empty")
    String sourceCardId,

    @NotBlank(message = "Destination card ID cannot be empty")
    String destinationCardId,

    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Amount must be positive")
    BigDecimal amount
) {}