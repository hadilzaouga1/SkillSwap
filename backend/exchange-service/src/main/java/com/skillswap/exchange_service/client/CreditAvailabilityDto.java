package com.skillswap.exchange_service.client;

/** Réponse de credit-service : solde d'un utilisateur (balance = possédé, available = utilisable). */
public record CreditAvailabilityDto(Long userId, int balance, int reserved, int available) {
}
