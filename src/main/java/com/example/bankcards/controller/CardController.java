package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<CardDto>> getUserCards(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) CardStatus status,
            @RequestParam(required = false) BigDecimal minBalance,
            @RequestParam(required = false) BigDecimal maxBalance,
            Pageable pageable
    ) {
        Page<Card> cards = cardService.getUserCards(userDetails.getUsername(), pageable);
        Page<CardDto> result = cards.map(this::toDto);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CardDto> getCardById(@PathVariable Long id) {
        Card card = cardService.getCardById(id);
        return ResponseEntity.ok(toDto(card));
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CardDto> createCard(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CardDto cardDto
    ) {
        Card card = new Card();
        card.setMaskedNumber(cardDto.getMaskedNumber());
        card.setBalance(cardDto.getBalance());
        card.setStatus(cardDto.getStatus());
        card.setExpirationDate(cardDto.getExpirationDate());

        Card saved = cardService.createCard(card, userDetails.getUsername());
        return ResponseEntity.ok(toDto(saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CardDto> updateCard(
            @PathVariable Long id,
            @Valid @RequestBody CardDto cardDto
    ) {
        Card updated = new Card();
        updated.setBalance(cardDto.getBalance());
        updated.setStatus(cardDto.getStatus());

        Card saved = cardService.updateCard(id, updated);
        return ResponseEntity.ok(toDto(saved));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.ok(Map.of("message", "Карта успешно удалена"));
    }

    private CardDto toDto(Card card) {
        CardDto dto = new CardDto();
        dto.setId(card.getId());
        dto.setMaskedNumber(maskCardNumber(card.getMaskedNumber()));
        dto.setOwnerName(card.getOwner().getUsername());
        dto.setExpirationDate(card.getExpirationDate());
        dto.setStatus(card.getStatus());
        dto.setBalance(card.getBalance());
        return dto;
    }

    private String maskCardNumber(String number) {
        if (number == null || number.length() < 4) return "****";
        return "**** **** **** " + number.substring(number.length() - 4);
    }
}
