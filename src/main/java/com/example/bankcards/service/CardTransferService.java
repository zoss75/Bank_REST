package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.security.UserPrincipal;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CardTransferService {

    private static final BigDecimal FREE_TRANSFER_LIMIT = BigDecimal.valueOf(100_000);
    private static final BigDecimal COMMISSION_RATE = BigDecimal.valueOf(0.001); // 0.1%

    private final CardRepository cardRepository;

    private Card findCard(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Карта с ID " + id + " не найдена"));
    }

    private void validateCardIsActive(Card card) {
        if (!card.getStatus().isActive()) {
            throw new IllegalArgumentException("Карта " + card.getId() + " неактивна или заблокирована");
        }
    }

    private BigDecimal calculateCommission(BigDecimal amount) {
        if (amount.compareTo(FREE_TRANSFER_LIMIT) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal overLimit = amount.subtract(FREE_TRANSFER_LIMIT);
        return overLimit.multiply(COMMISSION_RATE);
    }

    @Transactional
    public void transferBetweenOwnCards(UserPrincipal currentUser, Long fromCardId, Long toCardId, BigDecimal amount) {
        Card fromCard = findCard(fromCardId);
        Card toCard = findCard(toCardId);

        // Проверка принадлежности карт пользователю
        if (!fromCard.getOwner().getId().equals(currentUser.getId()) ||
                !toCard.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Вы можете переводить только между своими картами");
        }

        executeTransfer(fromCard, toCard, amount);
    }

    @Transactional
    public void transferBetweenAnyCards(Long fromCardId, Long toCardId, BigDecimal amount) {
        Card fromCard = findCard(fromCardId);
        Card toCard = findCard(toCardId);

        executeTransfer(fromCard, toCard, amount);
    }

    private void executeTransfer(Card fromCard, Card toCard, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма перевода должна быть положительной");
        }

        validateCardIsActive(fromCard);
        validateCardIsActive(toCard);

        BigDecimal commission = calculateCommission(amount);
        BigDecimal totalAmount = amount.add(commission);

        if (fromCard.getBalance().compareTo(totalAmount) < 0) {
            throw new IllegalArgumentException("Недостаточно средств для перевода");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(totalAmount));
        toCard.setBalance(toCard.getBalance().add(amount));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
    }
}
