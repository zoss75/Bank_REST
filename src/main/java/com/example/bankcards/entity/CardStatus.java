package com.example.bankcards.entity;

public enum CardStatus {
    ACTIVE,
    BLOCKED,
    DELETED;

    public boolean isActive() {
        return this == ACTIVE;
    }
}