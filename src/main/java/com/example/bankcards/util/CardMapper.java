package com.example.bankcards.util;

import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.entity.Card;
import org.springframework.stereotype.Component;

@Component
public class CardMapper {
    public CardResponse mapToCardResponse(Card card) {
        CardResponse.CardholderResponse holderDto = new CardResponse.CardholderResponse(
            card.getHolder().getId(),
            card.getHolder().getUsername()
        );

        String maskedNumber = "************" + card.getNumber().substring(12);

        return new CardResponse(
            card.getId(),
            maskedNumber,
            card.getExpiryDate(),
            card.getStatus(),
            card.getBalance(),
            holderDto
        );
    }
}
