package com.example.bankcards.controller;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.service.CardTransferService;
import com.example.bankcards.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cards")
public class CardTransferController {

    private final CardTransferService cardTransferService;

    public CardTransferController(CardTransferService cardTransferService) {
        this.cardTransferService = cardTransferService;
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> transferBetweenOwnCards(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody TransferRequest request
    ) {
        try {
            cardTransferService.transferBetweenOwnCards(
                    currentUser,
                    request.getFromCard(),
                    request.getToCard(),
                    request.getAmount()
            );

            return ResponseEntity.ok(Map.of(
                    "message", "Перевод успешно выполнен",
                    "fromCard", request.getFromCard(),
                    "toCard", request.getToCard(),
                    "amount", request.getAmount()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/transfer/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> adminTransfer(
            @Valid @RequestBody TransferRequest request
    ) {
        try {
            cardTransferService.transferBetweenAnyCards(
                    request.getFromCard(),
                    request.getToCard(),
                    request.getAmount()
            );

            return ResponseEntity.ok(Map.of(
                    "message", "Перевод администратором успешно выполнен",
                    "fromCard", request.getFromCard(),
                    "toCard", request.getToCard(),
                    "amount", request.getAmount()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
