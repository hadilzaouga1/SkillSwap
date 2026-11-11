package com.skillswap.credit_service.dto;

import com.skillswap.credit_service.entity.CreditTransaction;
import com.skillswap.credit_service.entity.TransactionType;

import java.time.LocalDateTime;

/** Une ligne de l'historique des crédits. */
public record TransactionResponse(Long id, TransactionType type, int amount, int balanceAfter,
                                  Long exchangeId, String description, LocalDateTime createdAt) {

    public static TransactionResponse from(CreditTransaction t) {
        return new TransactionResponse(t.getId(), t.getType(), t.getAmount(), t.getBalanceAfter(),
                t.getExchangeId(), t.getDescription(), t.getCreatedAt());
    }
}
