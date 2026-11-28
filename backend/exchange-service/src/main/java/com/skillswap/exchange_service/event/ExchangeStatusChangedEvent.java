package com.skillswap.exchange_service.event;

import com.skillswap.exchange_service.enums.ExchangeStatus;

import java.time.Instant;
import java.time.LocalDateTime;

public record ExchangeStatusChangedEvent(
        Long exchangeId,
        Long requesterId,
        Long providerId,
        Long skillId,
        String skillTitle,
        ExchangeStatus status,
        LocalDateTime proposedDateTime,
        String meetingLink,
        Instant occurredAt
) {
}