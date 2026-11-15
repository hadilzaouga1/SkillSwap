package com.skillswap.exchange_service.repository;

import com.skillswap.exchange_service.entity.Exchange;
import com.skillswap.exchange_service.enums.ExchangeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface ExchangeRepository extends JpaRepository<Exchange, Long> {

    boolean existsByRequesterIdAndSkillIdAndStatusIn(Long requesterId, Long skillId, Collection<ExchangeStatus> statuses);

    List<Exchange> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);

    List<Exchange> findByProviderIdOrderByCreatedAtDesc(Long providerId);

    // AJOUT : Trouve les échanges acceptés qui commencent dans 10 min et sans rappel envoyé
    List<Exchange> findByStatusAndReminderSentFalseAndProposedDateTimeBetween(
            ExchangeStatus status, LocalDateTime start, LocalDateTime end);
}