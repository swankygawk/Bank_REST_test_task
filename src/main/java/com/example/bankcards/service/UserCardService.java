package com.example.bankcards.service;

import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.specifications.CardSpecifications;
import com.example.bankcards.util.CardMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserCardService {
    private final CardRepository cardRepository;
    private final CardMapper cardMapper;

    @Transactional(readOnly = true)
    public Page<CardResponse> getUserCards(User user, CardStatus status, String searchTerm, Pageable pageable) {
        Specification<Card> spec = CardSpecifications.hasHolderId(user.getId());

        if (status != null) {
            spec = spec.and(CardSpecifications.hasStatus(status));
        }

        if (searchTerm != null && !searchTerm.isBlank()) {
            spec = spec.and(CardSpecifications.numberEndsWith(searchTerm));
        }

        Page<Card> cardsPage = cardRepository.findAll(spec, pageable);
        return cardsPage.map(cardMapper::mapToCardResponse);
    }

    @Transactional
    public CardResponse blockCard(User user, UUID id) {
        Card card = cardRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Card with ID " + id + " not found"));

        if (!card.getHolder().getId().equals(user.getId())) {
            throw new AccessDeniedException("This card does not belong to you");
        }

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new IllegalStateException("This card is already blocked");
        }

        card.setStatus(CardStatus.BLOCKED);
        Card blockedCard = cardRepository.save(card);
        return cardMapper.mapToCardResponse(blockedCard);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void transferMoney(User user, TransferRequest request) {
        if (request.sourceCardId().equals(request.destinationCardId())) {
            throw new IllegalArgumentException("Cannot transfer money to the same card");
        }

        UUID sourceCardId = UUID.fromString(request.sourceCardId());
        UUID destinationCardId = UUID.fromString(request.destinationCardId());

        List<Card> cardsList = cardRepository.findAllById(List.of(sourceCardId, destinationCardId));
        Map<UUID, Card> cardsMap = cardsList.stream()
            .collect(Collectors.toMap(Card::getId, Function.identity()));

        Card sourceCard = cardsMap.get(sourceCardId);
        if (sourceCard == null) {
            throw new EntityNotFoundException("Card with ID " + sourceCardId + " not found");
        }

        Card destinationCard = cardsMap.get(destinationCardId);
        if (destinationCard == null) {
            throw new EntityNotFoundException("Card with ID " + sourceCardId + " not found");
        }

        if (!sourceCard.getHolder().getId().equals(user.getId())
            || !destinationCard.getHolder().getId().equals(user.getId())
        ) {
            throw new AccessDeniedException("You can only transfer money between your cards");
        }

        if (sourceCard.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalStateException("Cannot transfer money due to source card being not active");
        }

        if (destinationCard.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalStateException("Cannot transfer money due to destination card being inactive");
        }

        if (sourceCard.getBalance().compareTo(request.amount()) < 0) {
            throw new IllegalStateException("Source card does not have enough money");
        }

        sourceCard.setBalance(sourceCard.getBalance().subtract(request.amount()));
        destinationCard.setBalance(destinationCard.getBalance().add(request.amount()));

        cardRepository.saveAll(List.of(sourceCard, destinationCard));
    }
}
