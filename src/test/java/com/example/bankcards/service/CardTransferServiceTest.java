package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardTransferServiceTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardTransferService cardTransferService;

    private User user;
    private Card cardFrom;
    private Card cardTo;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("user1")
                .build();

        cardFrom = Card.builder()
                .id(10L)
                .owner(user)
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(150_000))
                .build();

        cardTo = Card.builder()
                .id(20L)
                .owner(user)
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(50_000))
                .build();
    }

    @Test
    void shouldTransferWithoutCommission_WhenAmountBelowLimit() {
        // given
        when(cardRepository.findById(10L)).thenReturn(Optional.of(cardFrom));
        when(cardRepository.findById(20L)).thenReturn(Optional.of(cardTo));

        UserPrincipal principal = new UserPrincipal(user.getId(), user.getUsername(), "password", null);

        // when
        cardTransferService.transferBetweenOwnCards(principal, 10L, 20L, BigDecimal.valueOf(50_000));

        // then
        assertThat(cardFrom.getBalance()).isEqualByComparingTo("100000");
        assertThat(cardTo.getBalance()).isEqualByComparingTo("100000");
        verify(cardRepository, times(2)).save(any(Card.class));
    }

    @Test
    void shouldTransferWithCommission_WhenAmountAboveLimit() {
        // given
        when(cardRepository.findById(10L)).thenReturn(Optional.of(cardFrom));
        when(cardRepository.findById(20L)).thenReturn(Optional.of(cardTo));

        UserPrincipal principal = new UserPrincipal(user.getId(), user.getUsername(), "password", null);

        cardFrom.setBalance(BigDecimal.valueOf(150_150)); // баланс отправителя
        cardTo.setBalance(BigDecimal.ZERO); // получатель

        BigDecimal transferAmount = BigDecimal.valueOf(150_000);
        BigDecimal overLimit = transferAmount.subtract(BigDecimal.valueOf(100_000));
        BigDecimal commission = overLimit.multiply(BigDecimal.valueOf(0.001));

        BigDecimal expectedFromBalance = cardFrom.getBalance().subtract(transferAmount).subtract(commission);
        BigDecimal expectedToBalance = cardTo.getBalance().add(transferAmount);

        // when
        cardTransferService.transferBetweenOwnCards(principal, 10L, 20L, transferAmount);

        // then
        assertThat(cardFrom.getBalance()).isEqualByComparingTo(expectedFromBalance);
        assertThat(cardTo.getBalance()).isEqualByComparingTo(expectedToBalance);

        verify(cardRepository, times(2)).save(any(Card.class));
    }

    @Test
    void shouldThrowException_WhenInsufficientFunds() {
        // given
        cardFrom.setBalance(BigDecimal.valueOf(10_000));
        when(cardRepository.findById(10L)).thenReturn(Optional.of(cardFrom));
        when(cardRepository.findById(20L)).thenReturn(Optional.of(cardTo));

        UserPrincipal principal = new UserPrincipal(user.getId(), user.getUsername(), "password", null);

        // when / then
        assertThatThrownBy(() ->
                cardTransferService.transferBetweenOwnCards(principal, 10L, 20L, BigDecimal.valueOf(100_000))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Недостаточно средств");
    }

    @Test
    void shouldThrowException_WhenCardInactive() {
        // given
        cardFrom.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findById(10L)).thenReturn(Optional.of(cardFrom));
        when(cardRepository.findById(20L)).thenReturn(Optional.of(cardTo));

        UserPrincipal principal = new UserPrincipal(user.getId(), user.getUsername(), "password", null);

        // when / then
        assertThatThrownBy(() ->
                cardTransferService.transferBetweenOwnCards(principal, 10L, 20L, BigDecimal.valueOf(1000))
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("неактивна");
    }

    @Test
    void shouldThrowException_WhenCardNotOwnedByUser() {
        // given
        User anotherUser = User.builder().id(2L).username("other").build();
        cardFrom.setOwner(anotherUser);
        when(cardRepository.findById(10L)).thenReturn(Optional.of(cardFrom));
        when(cardRepository.findById(20L)).thenReturn(Optional.of(cardTo));

        UserPrincipal principal = new UserPrincipal(user.getId(), user.getUsername(), "password", null);

        // when / then
        assertThatThrownBy(() ->
                cardTransferService.transferBetweenOwnCards(principal, 10L, 20L, BigDecimal.valueOf(1000))
        ).isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
                .hasMessageContaining("только между своими картами");
    }
}