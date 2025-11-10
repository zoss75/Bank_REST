package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CardDto {
    private Long id;
    private String maskedNumber;
    private String ownerName;
    private LocalDate expirationDate;
    private CardStatus status;
    private BigDecimal balance;
}
