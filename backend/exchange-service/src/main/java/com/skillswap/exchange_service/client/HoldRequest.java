package com.skillswap.exchange_service.client;

/** Demande de blocage de crédits envoyée à credit-service. */
public record HoldRequest(Long exchangeId, Long payerId, Long payeeId, Integer amount) {
}
