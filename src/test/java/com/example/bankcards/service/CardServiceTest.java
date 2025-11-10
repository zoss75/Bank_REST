package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CardServiceTest {

    private CardRepository cardRepository;
    private UserRepository userRepository;
    private CardService cardService;

    private User user;
    private Card card1;
    private Card card2;

    @BeforeEach
    void setUp() {
        cardRepository = mock(CardRepository.class);
        userRepository = mock(UserRepository.class);
        cardService = new CardService(cardRepository, userRepository);

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        card1 = new Card();
        card1.setId(1L);
        card1.setOwner(user);
        card1.setBalance(BigDecimal.valueOf(1000));
        card1.setStatus(CardStatus.ACTIVE);

        card2 = new Card();
        card2.setId(2L);
        card2.setOwner(user);
        card2.setBalance(BigDecimal.valueOf(2000));
        card2.setStatus(CardStatus.BLOCKED);
    }

    @Test
    void getUserCards_ShouldReturnPageOfCards() {
        Pageable pageable = mock(Pageable.class);
        Page<Card> page = new PageImpl<>(List.of(card1, card2));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cardRepository.findByOwner(user, pageable)).thenReturn(page);

        Page<Card> result = cardService.getUserCards("testuser", pageable);

        assertThat(result.getContent()).containsExactly(card1, card2);
        verify(userRepository).findByUsername("testuser");
        verify(cardRepository).findByOwner(user, pageable);
    }

    @Test
    void getUserCards_ShouldThrow_WhenUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getUserCards("unknown", mock(Pageable.class)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Пользователь не найден");
    }

    @Test
    void getCardById_ShouldReturnCard() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card1));

        Card result = cardService.getCardById(1L);

        assertThat(result).isEqualTo(card1);
    }

    @Test
    void getCardById_ShouldThrow_WhenCardNotFound() {
        when(cardRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getCardById(10L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Карта не найдена");
    }

    @Test
    void createCard_ShouldAssignOwnerAndSave() {
        Card newCard = new Card();
        newCard.setBalance(BigDecimal.valueOf(500));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(cardRepository.save(any(Card.class))).thenAnswer(i -> i.getArgument(0));

        Card result = cardService.createCard(newCard, "testuser");

        assertThat(result.getOwner()).isEqualTo(user);
        verify(cardRepository).save(newCard);
    }

    @Test
    void createCard_ShouldThrow_WhenUserNotFound() {
        Card newCard = new Card();
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.createCard(newCard, "unknown"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Пользователь не найден");
    }

    @Test
    void updateCard_ShouldChangeStatusAndBalance() {
        Card updated = new Card();
        updated.setStatus(CardStatus.BLOCKED);
        updated.setBalance(BigDecimal.valueOf(999));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card1));
        when(cardRepository.save(any(Card.class))).thenAnswer(i -> i.getArgument(0));

        Card result = cardService.updateCard(1L, updated);

        assertThat(result.getStatus()).isEqualTo(CardStatus.BLOCKED);
        assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(999));
        verify(cardRepository).save(card1);
    }

    @Test
    void deleteCard_ShouldCallRepositoryDelete() {
        when(cardRepository.existsById(1L)).thenReturn(true);

        cardService.deleteCard(1L);

        verify(cardRepository).deleteById(1L);
    }

    @Test
    void deleteCard_ShouldThrow_WhenCardNotFound() {
        when(cardRepository.existsById(10L)).thenReturn(false);

        assertThatThrownBy(() -> cardService.deleteCard(10L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Карта не найдена");
    }

    @Test
    void filterCards_ShouldFilterByStatusAndBalance() {
        Pageable pageable = mock(Pageable.class);
        Page<Card> page = new PageImpl<>(List.of(card1, card2));
        when(cardRepository.findByOwner(user, pageable)).thenReturn(page);

        Page<Card> result = cardService.filterCards(user, CardStatus.ACTIVE, BigDecimal.valueOf(500), BigDecimal.valueOf(1500), pageable);

        assertThat(result.getContent()).containsExactly(card1);
    }
}
