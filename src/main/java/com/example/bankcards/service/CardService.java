package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    // Получение всех карт пользователя с фильтрацией
    public Page<Card> getUserCards(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден: " + username));

        return cardRepository.findByOwner(user, pageable);
    }

    // Получение одной карты по ID
    public Card getCardById(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Карта не найдена: id=" + id));
    }

    // Создание карты
    public Card createCard(Card card, String username) {
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден: " + username));

        card.setOwner(owner);
        return cardRepository.save(card);
    }

    // Обновление карты (например, блокировка или изменение баланса)
    public Card updateCard(Long id, Card updated) {
        Card card = getCardById(id);

        Optional.ofNullable(updated.getStatus()).ifPresent(card::setStatus);
        Optional.ofNullable(updated.getBalance()).ifPresent(card::setBalance);

        return cardRepository.save(card);
    }

    // Удаление карты
    public void deleteCard(Long id) {
        if (!cardRepository.existsById(id)) {
            throw new EntityNotFoundException("Карта не найдена: id=" + id);
        }
        cardRepository.deleteById(id);
    }

    public Page<Card> filterCards(User owner, CardStatus status, BigDecimal minBalance, BigDecimal maxBalance, Pageable pageable) {
        Page<Card> cards = cardRepository.findByOwner(owner, pageable);

        List<Card> filtered = cards.getContent().stream()
                .filter(card -> (status == null || card.getStatus() == status))
                .filter(card -> (minBalance == null || card.getBalance().compareTo(minBalance) >= 0))
                .filter(card -> (maxBalance == null || card.getBalance().compareTo(maxBalance) <= 0))
                .toList();

        return new PageImpl<>(filtered, pageable, filtered.size());
    }
}