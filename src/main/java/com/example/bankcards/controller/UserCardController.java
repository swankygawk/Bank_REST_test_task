package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.service.UserCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class UserCardController {
    private final UserCardService userCardService;

    @GetMapping
    public ResponseEntity<Page<CardResponse>> getUserCards(
        @AuthenticationPrincipal User user,
        @RequestParam(required = false) CardStatus status,
        @RequestParam(required = false, name = "search") String searchTerm,
        Pageable pageable
    ) {
        Page<CardResponse> cards = this.userCardService.getUserCards(user, status, searchTerm, pageable);
        return ResponseEntity.ok(cards);
    }

    @PatchMapping("/{id}/block")
    public ResponseEntity<CardResponse> blockCard(
        @AuthenticationPrincipal User user,
        @PathVariable UUID id
    ) {
        CardResponse blockedCard = this.userCardService.blockCard(user, id);
        return ResponseEntity.ok(blockedCard);
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transferMoney(
        @AuthenticationPrincipal User user,
        @RequestBody @Valid TransferRequest request
    ) {
        this.userCardService.transferMoney(user, request);
        return ResponseEntity.ok().build();
    }
}
