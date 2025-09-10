package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.CardStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record CardResponse(
   UUID id,
   String number,
   String expiryDate,
   CardStatus status,
   BigDecimal balance,
   UserDto holder
) {
    public record UserDto(
        UUID id,
        String username
    ) {}
}
