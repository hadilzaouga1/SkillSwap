package com.skillswap.credit_service.dto;

import com.skillswap.credit_service.entity.CreditAccount;

/** Solde d'un utilisateur : possédé, bloqué et disponible. */
public record BalanceResponse(Long userId, int balance, int reserved, int available) {

    public static BalanceResponse from(CreditAccount a) {
        return new BalanceResponse(a.getUserId(), a.getBalance(), a.getReserved(), a.available());
    }
}
