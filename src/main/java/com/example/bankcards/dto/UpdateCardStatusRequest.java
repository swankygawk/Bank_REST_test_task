package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.CardStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateCardStatusRequest(
    @NotNull(message = "New status cannot be null")
    CardStatus newStatus
) {}
