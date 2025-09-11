package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.dto.UpdateCardStatusRequest;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.service.AdminCardService;
import com.example.bankcards.util.WithMockCustomUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminCardControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminCardService adminCardService;

    @Test
    @WithMockCustomUser(roles = {"ADMIN"})
    void createCard_whenUserIsAdmin_shouldReturnCreated() throws Exception {
        CreateCardRequest request = new CreateCardRequest(
            UUID.randomUUID().toString(),
            "1111222233334444",
            "01/30",
            BigDecimal.TEN
        );

        when(adminCardService.createCard(any(CreateCardRequest.class))).thenReturn(
            new CardResponse(null, null, null, null, null, null)
        );

        mockMvc.perform(post("/api/v1/admin/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockCustomUser
    void createCard_whenUserIsNotAdmin_shouldReturnForbidden() throws Exception {
        CreateCardRequest request = new CreateCardRequest(
            UUID.randomUUID().toString(),
            "1111222233334444",
            "01/30",
            BigDecimal.TEN
        );

        mockMvc.perform(post("/api/v1/admin/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    void createCard_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/admin/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockCustomUser(roles = {"ADMIN"})
    void updateCardStatus_whenUserIsAdmin_shouldReturnOk() throws Exception {
        UUID cardId = UUID.randomUUID();
        UpdateCardStatusRequest request = new UpdateCardStatusRequest(CardStatus.BLOCKED);
        CardResponse response = new CardResponse(
            cardId,
            "",
            "",
            CardStatus.BLOCKED,
            BigDecimal.ZERO,
            null
        );

        when(adminCardService.updateCardStatus(eq(cardId), any(UpdateCardStatusRequest.class)))
            .thenReturn(response);

        mockMvc.perform(patch("/api/v1/admin/cards/{id}/status", cardId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    @WithMockCustomUser(roles = {"ADMIN"})
    void deleteCard_whenUserIsAdmin_shouldReturnNoContent() throws Exception {
        UUID cardId = UUID.randomUUID();

        doNothing().when(adminCardService).deleteCard(cardId);

        mockMvc.perform(delete("/api/v1/admin/cards/{id}", cardId))
            .andExpect(status().isNoContent());

        verify(adminCardService, times(1)).deleteCard(cardId);
    }
}
