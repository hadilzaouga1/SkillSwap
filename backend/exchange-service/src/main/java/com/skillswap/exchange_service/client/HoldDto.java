package com.skillswap.exchange_service.client;

/** Réponse de credit-service : état de la réservation (HELD, TRANSFERRED, RELEASED). */
public record HoldDto(Long exchangeId, Long payerId, Long payeeId, int amount, String status) {
}
