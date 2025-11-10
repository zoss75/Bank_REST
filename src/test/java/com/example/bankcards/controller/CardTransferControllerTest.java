package com.example.bankcards.controller;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.security.UserPrincipal;
import com.example.bankcards.service.CardTransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CardTransferControllerTest {

    private CardTransferService cardTransferService;
    private CardTransferController controller;

    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        cardTransferService = mock(CardTransferService.class);
        controller = new CardTransferController(cardTransferService);

        userPrincipal = new UserPrincipal(1L, "testuser", "password", null);
    }

    @Test
    void transferBetweenOwnCards_ShouldReturnSuccessMessage() {
        TransferRequest request = new TransferRequest();
        request.setFromCard(10L);
        request.setToCard(20L);
        request.setAmount(BigDecimal.valueOf(500));

        ResponseEntity<?> response = controller.transferBetweenOwnCards(userPrincipal, request);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).containsEntry("message", "Перевод успешно выполнен")
                .containsEntry("fromCard", 10L)
                .containsEntry("toCard", 20L)
                .containsEntry("amount", BigDecimal.valueOf(500));

        verify(cardTransferService).transferBetweenOwnCards(userPrincipal, 10L, 20L, BigDecimal.valueOf(500));
    }

    @Test
    void transferBetweenOwnCards_ShouldReturnErrorMessage_WhenServiceThrows() {
        TransferRequest request = new TransferRequest();
        request.setFromCard(10L);
        request.setToCard(20L);
        request.setAmount(BigDecimal.valueOf(500));

        doThrow(new IllegalArgumentException("Недостаточно средств")).when(cardTransferService)
                .transferBetweenOwnCards(userPrincipal, 10L, 20L, BigDecimal.valueOf(500));

        ResponseEntity<?> response = controller.transferBetweenOwnCards(userPrincipal, request);

        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body).containsEntry("error", "Недостаточно средств");
    }

    @Test
    void adminTransfer_ShouldReturnSuccessMessage() {
        TransferRequest request = new TransferRequest();
        request.setFromCard(100L);
        request.setToCard(200L);
        request.setAmount(BigDecimal.valueOf(1000));

        ResponseEntity<?> response = controller.adminTransfer(request);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).containsEntry("message", "Перевод администратором успешно выполнен")
                .containsEntry("fromCard", 100L)
                .containsEntry("toCard", 200L)
                .containsEntry("amount", BigDecimal.valueOf(1000));

        verify(cardTransferService).transferBetweenAnyCards(100L, 200L, BigDecimal.valueOf(1000));
    }

    @Test
    void adminTransfer_ShouldReturnErrorMessage_WhenServiceThrows() {
        TransferRequest request = new TransferRequest();
        request.setFromCard(100L);
        request.setToCard(200L);
        request.setAmount(BigDecimal.valueOf(1000));

        doThrow(new IllegalArgumentException("Ошибка перевода")).when(cardTransferService)
                .transferBetweenAnyCards(100L, 200L, BigDecimal.valueOf(1000));

        ResponseEntity<?> response = controller.adminTransfer(request);

        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body).containsEntry("error", "Ошибка перевода");
    }
}