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
   CardholderResponse holder
) {
    public record CardholderResponse(
        UUID id,
        String username
    ) {}
}
