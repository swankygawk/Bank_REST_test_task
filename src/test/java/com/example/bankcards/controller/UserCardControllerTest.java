package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.service.UserCardService;
import com.example.bankcards.util.WithMockCustomUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserCardControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserCardService userCardService;

    @Test
    @WithMockCustomUser
    void getUserCards_whenAuthenticated_shouldReturnCardsPage() throws Exception {
        String userIdStr = WithMockCustomUser.class.getMethod("id").getDefaultValue().toString();
        UUID userId = UUID.fromString(userIdStr);
        String username = WithMockCustomUser.class.getMethod("username").getDefaultValue().toString();

        CardResponse.UserDto userDto = new CardResponse.UserDto(userId, username);
        Page<CardResponse> cards = new PageImpl<>(List.of(
            new CardResponse(
                UUID.randomUUID(),
                "",
                "",
                CardStatus.ACTIVE,
                BigDecimal.TEN, userDto
            )
        ));

        when(userCardService.getUserCards(any(User.class), any(), any(), any(Pageable.class)))
            .thenReturn(cards);

        mockMvc.perform(get("/api/v1/cards"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].holder.id").value(userId.toString()));
    }

    @Test
    void getUserCards_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/cards")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockCustomUser
    void blockCard_whenCardExistsAndIsOwned_shouldReturnOk() throws Exception {
        UUID cardId = UUID.randomUUID();
        CardResponse response = new CardResponse(
            cardId,
            "",
            "",
            CardStatus.BLOCKED,
            BigDecimal.ZERO,
            null
        );

        when(userCardService.blockCard(any(User.class), eq(cardId))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/cards/{id}/block", cardId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(cardId.toString()))
            .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    @WithMockCustomUser
    void transferMoney_whenRequestIsValid_shouldReturnOk() throws Exception {
        TransferRequest request = new TransferRequest(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            new BigDecimal("100.00")
        );

        mockMvc.perform(post("/api/v1/cards/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        verify(userCardService, times(1)).transferMoney(any(User.class), any(TransferRequest.class));
    }

    @Test
    @WithMockCustomUser
    void transferMoney_whenAmountIsNegative_shouldReturnBadRequest() throws Exception {
        TransferRequest badRequest = new TransferRequest(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            new BigDecimal("-100.00")
        );

        mockMvc.perform(post("/api/v1/cards/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(badRequest)))
            .andExpect(status().isBadRequest());
    }
}
