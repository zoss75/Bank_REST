package com.example.bankcards.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class TransferRequest {

    @NotNull(message = "ID карты-отправителя обязательно")
    private Long fromCard;

    @NotNull(message = "ID карты-получателя обязательно")
    private Long toCard;

    @NotNull(message = "Сумма обязательна")
    @DecimalMin(value = "0.01", message = "Сумма перевода должна быть больше 0")
    private BigDecimal amount;

    public Long getFromCard() {
        return fromCard;
    }

    public void setFromCard(Long fromCard) {
        this.fromCard = fromCard;
    }

    public Long getToCard() {
        return toCard;
    }

    public void setToCard(Long toCard) {
        this.toCard = toCard;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}