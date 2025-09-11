package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.dto.UpdateCardStatusRequest;
import com.example.bankcards.service.AdminCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/cards")
@Validated
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminCardController {
    private final AdminCardService adminCardService;

    @GetMapping
    public ResponseEntity<Page<CardResponse>> getAllCards(Pageable pageable) {
        Page<CardResponse> cards = adminCardService.getAllCards(pageable);
        return ResponseEntity.ok(cards);
    }

    @PostMapping
    public ResponseEntity<CardResponse> createCard(@RequestBody @Valid CreateCardRequest request) {
        CardResponse createdCard = this.adminCardService.createCard(request);
        return new ResponseEntity<>(createdCard, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CardResponse> updateCardStatus(
        @PathVariable UUID id,
        @RequestBody @Valid UpdateCardStatusRequest request
    ) {
        CardResponse updatedCard = this.adminCardService.updateCardStatus(id, request);
        return ResponseEntity.ok(updatedCard);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCard(@PathVariable UUID id) {
        this.adminCardService.deleteCard(id);
    }
}
