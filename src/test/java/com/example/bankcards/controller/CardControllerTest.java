package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CardControllerTest {

    private CardService cardService;
    private CardController cardController;

    private UserDetails userDetails;
    private Card card;

    @BeforeEach
    void setUp() {
        cardService = mock(CardService.class);
        cardController = new CardController(cardService);

        userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("testuser");

        User owner = new User();
        owner.setUsername("testuser");

        card = new Card();
        card.setId(1L);
        card.setMaskedNumber("1234567812345678");
        card.setOwner(owner);
        card.setBalance(BigDecimal.valueOf(1000));
        card.setStatus(CardStatus.ACTIVE);
        card.setExpirationDate(LocalDate.of(2030, 12, 31));
    }

    @Test
    void getUserCards_ShouldReturnPageOfCardDto() {
        Pageable pageable = mock(Pageable.class);
        Page<Card> page = new PageImpl<>(List.of(card));

        when(cardService.getUserCards("testuser", pageable)).thenReturn(page);

        ResponseEntity<Page<CardDto>> response = cardController.getUserCards(userDetails, null, null, null, pageable);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getId()).isEqualTo(1L);
        assertThat(response.getBody().getContent().get(0).getMaskedNumber()).isEqualTo("**** **** **** 5678");
        verify(cardService).getUserCards("testuser", pageable);
    }

    @Test
    void getCardById_ShouldReturnCardDto() {
        when(cardService.getCardById(1L)).thenReturn(card);

        ResponseEntity<CardDto> response = cardController.getCardById(1L);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        assertThat(response.getBody().getMaskedNumber()).isEqualTo("**** **** **** 5678");
        verify(cardService).getCardById(1L);
    }

    @Test
    void createCard_ShouldReturnSavedCardDto() {
        CardDto requestDto = new CardDto();
        requestDto.setMaskedNumber("1234567812345678");
        requestDto.setBalance(BigDecimal.valueOf(500));
        requestDto.setStatus(CardStatus.ACTIVE);
        requestDto.setExpirationDate(LocalDate.of(2030, 12, 31));

        when(cardService.createCard(any(Card.class), eq("testuser"))).thenReturn(card);

        ResponseEntity<CardDto> response = cardController.createCard(userDetails, requestDto);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        assertThat(response.getBody().getMaskedNumber()).isEqualTo("**** **** **** 5678");

        ArgumentCaptor<Card> captor = ArgumentCaptor.forClass(Card.class);
        verify(cardService).createCard(captor.capture(), eq("testuser"));
        assertThat(captor.getValue().getMaskedNumber()).isEqualTo("1234567812345678");
    }

    @Test
    void updateCard_ShouldReturnUpdatedCardDto() {
        CardDto requestDto = new CardDto();
        requestDto.setBalance(BigDecimal.valueOf(2000));
        requestDto.setStatus(CardStatus.BLOCKED);

        when(cardService.updateCard(eq(1L), any(Card.class))).thenReturn(card);

        ResponseEntity<CardDto> response = cardController.updateCard(1L, requestDto);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        verify(cardService).updateCard(eq(1L), any(Card.class));
    }

    @Test
    void deleteCard_ShouldReturnSuccessMessage() {
        ResponseEntity<Map<String, String>> response = cardController.deleteCard(1L);

        assertThat(response.getBody()).containsEntry("message", "Карта успешно удалена");
        verify(cardService).deleteCard(1L);
    }

    @Test
    void maskCardNumber_ShouldMaskProperly() throws Exception {
        // Тестируем приватный метод через reflection
        var method = CardController.class.getDeclaredMethod("maskCardNumber", String.class);
        method.setAccessible(true);
        String masked = (String) method.invoke(cardController, "1234567812345678");
        assertThat(masked).isEqualTo("**** **** **** 5678");

        masked = (String) method.invoke(cardController, "1234");
        assertThat(masked).isEqualTo("**** **** **** 1234");

        masked = (String) method.invoke(cardController, "12");
        assertThat(masked).isEqualTo("****");
    }
}
