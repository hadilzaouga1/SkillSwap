package com.skillswap.exchange_service.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

// Le demandeur et le prestataire ne sont PAS dans la requête :
// le demandeur vient du JWT (X-User-Id), le prestataire vient de la compétence.
public record ExchangeRequest(
        @NotNull(message = "skillId est obligatoire")
        Long skillId,

        @Size(max = 1000, message = "La description ne doit pas dépasser 1000 caractères")
        String description,

        @NotNull(message = "durationHours est obligatoire")
        @Min(value = 1, message = "La durée doit être d'au moins 1 heure")
        Integer durationHours,

        @NotNull(message = "proposedDateTime est obligatoire")
        @Future(message = "Le créneau doit être dans le futur")
        LocalDateTime proposedDateTime
) {}
