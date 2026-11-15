package com.skillswap.exchange_service.dto;

import com.skillswap.exchange_service.entity.Exchange;
import com.skillswap.exchange_service.enums.ExchangeStatus;

import java.time.LocalDateTime;

public record ExchangeResponse(
        Long id,
        Long requesterId,
        Long providerId,
        Long skillId,
        String skillTitle,
        String description,
        Integer durationHours,
        LocalDateTime proposedDateTime,
        ExchangeStatus status,
        String meetingLink,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ExchangeResponse from(Exchange e) {
        return new ExchangeResponse(
                e.getId(),
                e.getRequesterId(),
                e.getProviderId(),
                e.getSkillId(),
                e.getSkillTitle(),
                e.getDescription(),
                e.getDurationHours(),
                e.getProposedDateTime(),
                e.getStatus(),
                e.getMeetingLink(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}