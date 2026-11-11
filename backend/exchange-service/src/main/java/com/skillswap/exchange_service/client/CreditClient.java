package com.skillswap.exchange_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Appels vers credit-service (trouvé par son nom dans Eureka).
 * Les routes /internal/** ne passent pas par la Gateway : communication entre services uniquement.
 * Toutes les opérations sont idempotentes : on peut les renvoyer sans risque après une erreur.
 */
@FeignClient(name = "credit-service", configuration = CreditClientConfig.class)
public interface CreditClient {

    /** Crédits possédés / bloqués / disponibles d'un utilisateur. */
    @GetMapping("/internal/credits/{userId}/availability")
    CreditAvailabilityDto getAvailability(@PathVariable("userId") Long userId);

    /** Bloque les crédits du demandeur (échange accepté). */
    @PostMapping("/internal/credits/holds")
    HoldDto reserve(@RequestBody HoldRequest request);

    /** Débloque les crédits (échange annulé). */
    @PostMapping("/internal/credits/holds/{exchangeId}/release")
    void release(@PathVariable("exchangeId") Long exchangeId);

    /** Verse les crédits au prestataire (échange confirmé). */
    @PostMapping("/internal/credits/holds/{exchangeId}/transfer")
    HoldDto transfer(@PathVariable("exchangeId") Long exchangeId);
}
