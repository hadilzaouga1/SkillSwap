package com.skillswap.credit_service.controller;

import com.skillswap.credit_service.dto.BalanceResponse;
import com.skillswap.credit_service.dto.TransactionResponse;
import com.skillswap.credit_service.exception.ApiException;
import com.skillswap.credit_service.service.CreditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Routes utilisateur : /api/credits/** (passent par la Gateway, qui exige un token valide).
 * L'identité vient UNIQUEMENT de l'en-tête X-User-Id posé par la Gateway.
 */
@RestController
@RequestMapping("/api/credits")
@Tag(name = "Crédits")
public class CreditController {

    private static final String USER_HEADER = "X-User-Id";

    private final CreditService creditService;

    public CreditController(CreditService creditService) {
        this.creditService = creditService;
    }

    @Operation(summary = "Mon solde : possédé, bloqué, disponible (US14)")
    @GetMapping("/balance")
    public BalanceResponse balance(@RequestHeader(value = USER_HEADER, required = false) String userId) {
        return creditService.getBalance(userId(userId));
    }

    @Operation(summary = "Historique de mes crédits (US14)")
    @GetMapping("/transactions")
    public List<TransactionResponse> transactions(
            @RequestHeader(value = USER_HEADER, required = false) String userId) {
        return creditService.getTransactions(userId(userId));
    }

    private Long userId(String header) {
        if (header == null || header.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentification requise");
        }
        try {
            return Long.parseLong(header);
        } catch (NumberFormatException ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Identité invalide");
        }
    }
}
