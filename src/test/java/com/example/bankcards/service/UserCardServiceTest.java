package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.CardMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserCardServiceTest {
    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardMapper cardMapper;

    @InjectMocks
    private UserCardService userCardService;

    @Test
    void getUserCards_shouldCallRepositoryWithCorrectSpecAndMapResult() {
        User user = User.builder()
            .id(UUID.randomUUID())
            .build();
        Pageable pageable = PageRequest.of(0, 10);

        Page<Card> cardPage = new PageImpl<>(List.of(new Card(), new Card()));

        when(cardRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(cardPage);

        userCardService.getUserCards(user, null, null, pageable);

        verify(cardRepository).findAll(any(Specification.class), eq(pageable));
        verify(cardMapper, times(2)).mapToCardResponse(any(Card.class));
    }

    @Test
    void blockCard_whenCardIsOwnedAndActive_shouldBlockCard() {
        User user = User.builder()
            .id(UUID.randomUUID())
            .build();

        Card card = Card.builder()
            .id(UUID.randomUUID())
            .status(CardStatus.ACTIVE)
            .holder(user)
            .build();

        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        userCardService.blockCard(user, card.getId());

        ArgumentCaptor<Card> cardArgumentCaptor = ArgumentCaptor.forClass(Card.class);
        verify(cardRepository).save(cardArgumentCaptor.capture());

        Card savedCard = cardArgumentCaptor.getValue();
        assertThat(savedCard.getStatus()).isEqualTo(CardStatus.BLOCKED);
    }

    @Test
    void blockCard_whenCardNotFound_shouldThrowException() {
        UUID nonExistentCardId = UUID.randomUUID();
        User user = User.builder()
            .id(UUID.randomUUID())
            .build();

        when(cardRepository.findById(nonExistentCardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userCardService.blockCard(user, nonExistentCardId))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Card with ID " + nonExistentCardId + " not found");

        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void blockCard_whenCardIsNotOwned_shouldThrowException() {
        User cardHolder = User.builder()
            .id(UUID.randomUUID())
            .build();

        User user = User.builder()
            .id(UUID.randomUUID())
            .build();

        Card card = Card.builder()
            .id(UUID.randomUUID())
            .status(CardStatus.ACTIVE)
            .holder(cardHolder)
            .build();

        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> userCardService.blockCard(user, card.getId()))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("This card does not belong to you");

        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void blockCard_whenCardIsAlreadyBlocked_shouldThrowException() {
        User user = User.builder()
            .id(UUID.randomUUID())
            .build();

        Card card = Card.builder()
            .id(UUID.randomUUID())
            .status(CardStatus.BLOCKED)
            .holder(user)
            .build();

        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> userCardService.blockCard(user, card.getId()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("This card is already blocked");

        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void transferMoney_whenSuccessful_shouldUpdateBalanceAndSaveChanges() {
        User user = User.builder()
            .id(UUID.randomUUID())
            .build();

        Card sourceCard = Card.builder()
            .id(UUID.randomUUID())
            .status(CardStatus.ACTIVE)
            .balance(new BigDecimal("1000.00"))
            .holder(user)
            .build();

        Card destinationCard = Card.builder()
            .id(UUID.randomUUID())
            .status(CardStatus.ACTIVE)
            .balance(new BigDecimal("500.00"))
            .holder(user)
            .build();

        TransferRequest request = new TransferRequest(
            sourceCard.getId().toString(),
            destinationCard.getId().toString(),
            new BigDecimal("200.00")
        );

        when(cardRepository.findAllById(anyList())).thenReturn(List.of(sourceCard, destinationCard));

        userCardService.transferMoney(user, request);

        assertThat(sourceCard.getBalance()).isEqualByComparingTo("800.00");
        assertThat(destinationCard.getBalance()).isEqualByComparingTo("700.00");

        verify(cardRepository, times(1)).saveAll(anyList());
    }

    @Test
    void transferMoney_whenTransferringToSameCard_shouldThrowException() {
        User user = User.builder()
            .id(UUID.randomUUID())
            .build();

        Card sourceCard = Card.builder()
            .id(UUID.randomUUID())
            .build();

        TransferRequest request = new TransferRequest(
            sourceCard.getId().toString(),
            sourceCard.getId().toString(),
            new BigDecimal("200.00")
        );

        assertThatThrownBy(() -> userCardService.transferMoney(user, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cannot transfer money to the same card");

        verify(cardRepository, never()).findAllById(anyList());
        verify(cardRepository, never()).saveAll(anyList());
    }

    @Test
    void transferMoney_whenOneCardIsNotFound_shouldThrowException() {
        User user = User.builder()
            .id(UUID.randomUUID())
            .build();

        UUID foundCardId = UUID.randomUUID();
        UUID notFoundCardId = UUID.randomUUID();

        TransferRequest request = new TransferRequest(
            foundCardId.toString(),
            notFoundCardId.toString(),
            new BigDecimal("200.00")
        );

        Card foundCard = Card.builder()
            .id(foundCardId)
            .build();

        when(cardRepository.findAllById(anyList())).thenReturn(List.of(foundCard));

        assertThatThrownBy(() -> userCardService.transferMoney(user, request))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Card with ID " + notFoundCardId + " not found");
    }

    @Test
    void transferMoney_whenCardIsNotOwned_shouldThrowException() {
        User firstUser = User.builder()
            .id(UUID.randomUUID())
            .build();

        User secondUser = User.builder()
            .id(UUID.randomUUID())
            .build();

        Card sourceCard = Card.builder()
            .id(UUID.randomUUID())
            .holder(firstUser)
            .build();

        Card destinationCard = Card.builder()
            .id(UUID.randomUUID())
            .holder(secondUser)
            .build();

        TransferRequest request = new TransferRequest(
            sourceCard.getId().toString(),
            destinationCard.getId().toString(),
            new BigDecimal("200.00")
        );

        when(cardRepository.findAllById(anyList())).thenReturn(List.of(sourceCard, destinationCard));

        assertThatThrownBy(() -> userCardService.transferMoney(firstUser, request))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("You can only transfer money between your cards");

        verify(cardRepository, never()).saveAll(anyList());
    }

    @Test
    void transferMoney_whenCardIsNotActive_shouldThrowException() {
        User user = User.builder()
            .id(UUID.randomUUID())
            .build();

        Card sourceCard = Card.builder()
            .id(UUID.randomUUID())
            .status(CardStatus.BLOCKED)
            .holder(user)
            .build();

        Card destinationCard = Card.builder()
            .id(UUID.randomUUID())
            .status(CardStatus.ACTIVE)
            .holder(user)
            .build();

        TransferRequest request = new TransferRequest(
            sourceCard.getId().toString(),
            destinationCard.getId().toString(),
            new BigDecimal("200.00")
        );

        when(cardRepository.findAllById(anyList())).thenReturn(List.of(sourceCard, destinationCard));

        assertThatThrownBy(() -> userCardService.transferMoney(user, request))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot transfer money due to source or destination card being not active");

        verify(cardRepository, never()).saveAll(anyList());
    }

    @Test
    void transferMoney_whenInsufficientFunds_shouldThrowException() {
        User user = User.builder()
            .id(UUID.randomUUID())
            .build();

        Card sourceCard = Card.builder()
            .id(UUID.randomUUID())
            .status(CardStatus.ACTIVE)
            .balance(new BigDecimal("100.00"))
            .holder(user)
            .build();

        Card destinationCard = Card.builder()
            .id(UUID.randomUUID())
            .status(CardStatus.ACTIVE)
            .balance(new BigDecimal("500.00"))
            .holder(user)
            .build();

        TransferRequest request = new TransferRequest(
            sourceCard.getId().toString(),
            destinationCard.getId().toString(),
            new BigDecimal("200.00")
        );

        when(cardRepository.findAllById(anyList())).thenReturn(List.of(sourceCard, destinationCard));

        assertThatThrownBy(() -> userCardService.transferMoney(user, request))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Insufficient funds");
        assertThat(sourceCard.getBalance()).isEqualByComparingTo("100.00");

        verify(cardRepository, never()).saveAll(anyList());
    }

}
