package com.skillswap.exchange_service.client;

public record ReminderRequest(
        Long exchangeId,
        Long requesterId,
        Long providerId,
        String skillTitle,
        String meetingLink
) {}