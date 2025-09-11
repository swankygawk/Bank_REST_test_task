package com.example.bankcards.service;

import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.dto.UpdateCardStatusRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminCardServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardMapper cardMapper;

    @InjectMocks
    private AdminCardService adminCardService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(adminCardService, "cryptoSecret", "a6d8a1eb2314b9b3944beb67d3cea119");
    }

    @Test
    void createCard_whenDataIsValid_shouldCreateAndSaveCard() {
        User user = User.builder()
            .id(UUID.randomUUID())
            .build();

        CreateCardRequest request = new CreateCardRequest(
            user.getId().toString(),
            "1111222233334444",
            "01/30",
            BigDecimal.TEN
        );

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(cardRepository.existsByNumberHash(anyString())).thenReturn(false);
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cardMapper.mapToCardResponse(any(Card.class))).thenReturn((null));

        adminCardService.createCard(request);

        verify(cardRepository).existsByNumberHash(anyString());
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void createCard_whenUserNotFound_shouldThrowException() {
        UUID nonExistentUserId = UUID.randomUUID();

        CreateCardRequest request = new CreateCardRequest(
            nonExistentUserId.toString(),
            "1111222233334444",
            "01/30",
            BigDecimal.TEN
        );

        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminCardService.createCard(request))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("User with ID " + nonExistentUserId + " not found");

        verify(cardRepository, never()).existsByNumberHash(anyString());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void createCard_whenNumberExists_shouldThrowException() {
        User user = User.builder()
            .id(UUID.randomUUID())
            .build();

        CreateCardRequest request = new CreateCardRequest(
            user.getId().toString(),
            "1111222233334444",
            "01/30",
            BigDecimal.TEN
        );

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(cardRepository.existsByNumberHash(anyString())).thenReturn(true);

        assertThatThrownBy(() -> adminCardService.createCard(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Number " + request.number() + " is already taken");

        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void updateCardStatus_whenStatusIsNotExpired_shouldUpdateStatus() {
        Card card = Card.builder()
            .id(UUID.randomUUID())
            .status(CardStatus.ACTIVE)
            .build();
        UpdateCardStatusRequest request = new UpdateCardStatusRequest(CardStatus.BLOCKED);

        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cardMapper.mapToCardResponse(any(Card.class))).thenReturn(null);

        adminCardService.updateCardStatus(card.getId(), request);

        ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);
        verify(cardRepository).save(cardCaptor.capture());

        assertThat(cardCaptor.getValue().getStatus()).isEqualTo(CardStatus.BLOCKED);
    }

    @Test
    void updateCardStatus_whenCardNotFound_shouldThrowException() {
        UUID nonExistentCardId = UUID.randomUUID();
        UpdateCardStatusRequest request = new UpdateCardStatusRequest(CardStatus.BLOCKED);

        when(cardRepository.findById(nonExistentCardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminCardService.updateCardStatus(nonExistentCardId, request))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Card with ID " + nonExistentCardId + " not found");

        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void updateCardStatus_whenStatusIsExpired_shouldThrowException() {
        Card card = Card.builder()
            .id(UUID.randomUUID())
            .status(CardStatus.ACTIVE)
            .build();
        UpdateCardStatusRequest request = new UpdateCardStatusRequest(CardStatus.EXPIRED);

        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> adminCardService.updateCardStatus(card.getId(), request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cannot expire card manually");

        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void deleteCard_whenCardExists_shouldCallDeleteById() {
        UUID cardId = UUID.randomUUID();
        when(cardRepository.existsById(cardId)).thenReturn(true);

        adminCardService.deleteCard(cardId);

        verify(cardRepository).deleteById(cardId);
    }

    @Test
    void deleteCard_whenCardDoesNotExist_shouldThrowException() {
        UUID cardId = UUID.randomUUID();
        when(cardRepository.existsById(cardId)).thenReturn(false);

        assertThatThrownBy(() -> adminCardService.deleteCard(cardId))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Card with ID " + cardId + " not found");
    }
}
