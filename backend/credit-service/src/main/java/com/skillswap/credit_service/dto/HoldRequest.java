package com.skillswap.credit_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Demande de réservation envoyée par exchange-service quand un échange est accepté. */
public record HoldRequest(
        @NotNull(message = "exchangeId obligatoire") Long exchangeId,
        @NotNull(message = "payerId obligatoire") Long payerId,
        @NotNull(message = "payeeId obligatoire") Long payeeId,
        @NotNull(message = "amount obligatoire") @Min(value = 1, message = "Le montant doit être d'au moins 1 crédit") Integer amount
) {
}
