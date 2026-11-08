package com.skillswap.exchange_service.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ProposeTimeRequest(
        @NotNull(message = "proposedDateTime est obligatoire")
        @Future(message = "Le créneau doit être dans le futur")
        LocalDateTime proposedDateTime
) {}
