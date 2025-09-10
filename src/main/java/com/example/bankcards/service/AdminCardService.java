package com.example.bankcards.service;

import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.dto.UpdateCardStatusRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminCardService {
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final CardMapper cardMapper;

    @Transactional
    public CardResponse createCard(CreateCardRequest request) {
        User holder = userRepository.findById(UUID.fromString(request.userId()))
            .orElseThrow(() -> new EntityNotFoundException("User with ID " + request.userId() + " not found"));

        if (cardRepository.existsByNumber(request.number())) {
            throw new IllegalArgumentException("Number " + request.number() + " is already taken");
        }

        Card newCard = Card.builder()
            .number(request.number())
            .expiryDate(request.expiryDate())
            .status(CardStatus.ACTIVE)
            .balance(request.initialBalance())
            .holder(holder)
            .build();

        Card savedCard = cardRepository.save(newCard);

        return cardMapper.mapToCardResponse(savedCard);
    }

    @Transactional
    public CardResponse updateCardStatus(UUID id, UpdateCardStatusRequest request) {
        Card card = cardRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Card with ID " + id + " not found"));

        card.setStatus(request.newStatus());
        Card updatedCard = cardRepository.save(card);

        return cardMapper.mapToCardResponse(updatedCard);
    }

    @Transactional
    public void deleteCard(UUID id) {
        if (!cardRepository.existsById(id)) {
            throw new EntityNotFoundException("Card with ID " + id + " not found");
        }
        cardRepository.deleteById(id);
    }
}
