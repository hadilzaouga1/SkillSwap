package com.skillswap.credit_service.controller;

import com.skillswap.credit_service.dto.BalanceResponse;
import com.skillswap.credit_service.dto.HoldRequest;
import com.skillswap.credit_service.dto.HoldResponse;
import com.skillswap.credit_service.service.CreditService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Routes INTERNES appelées par exchange-service : /internal/credits/**
 *  - non exposées par la Gateway ;
 *  - protégées par la clé X-Internal-Key (voir InternalKeyInterceptor).
 */
@Hidden
@RestController
@RequestMapping("/internal/credits")
public class InternalCreditController {

    private final CreditService creditService;

    public InternalCreditController(CreditService creditService) {
        this.creditService = creditService;
    }

    /** Solde d'un utilisateur (vérification à la demande d'échange). */
    @GetMapping("/{userId}/availability")
    public BalanceResponse availability(@PathVariable("userId") Long userId) {
        return creditService.getBalance(userId);
    }

    /** Bloque les crédits du demandeur (échange accepté). Idempotent. */
    @PostMapping("/holds")
    @ResponseStatus(HttpStatus.CREATED)
    public HoldResponse reserve(@Valid @RequestBody HoldRequest request) {
        return creditService.reserve(request);
    }

    /** Débloque les crédits (échange annulé). Idempotent. */
    @PostMapping("/holds/{exchangeId}/release")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void release(@PathVariable("exchangeId") Long exchangeId) {
        creditService.release(exchangeId);
    }

    /** Verse les crédits au prestataire (échange confirmé). Idempotent. */
    @PostMapping("/holds/{exchangeId}/transfer")
    public HoldResponse transfer(@PathVariable("exchangeId") Long exchangeId) {
        return creditService.transfer(exchangeId);
    }
}
