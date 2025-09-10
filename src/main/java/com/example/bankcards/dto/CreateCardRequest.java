package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreateCardRequest(
    @NotBlank(message = "User ID cannot be empty")
    String userId,

    @NotBlank(message = "Number cannot be empty")
    @Pattern(
        regexp = "^\\d{16}$",
        message = "Number must be 16 digits"
    )
    String number,

    @NotBlank(message = "Expiry date cannot be empty")
    @Pattern(
        regexp = "^(0[1-9]|1[0-2])/(\\d{2})$",
        message = "Expiry date must be formatted as MM/YY"
    )
    String expiryDate,

    @NotNull(message = "Initial balance cannot be null")
    @PositiveOrZero(message = "Initial balance cannot be negative")
    BigDecimal initialBalance
) {}
