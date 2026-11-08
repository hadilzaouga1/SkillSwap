package com.skillswap.exchange_service.repository;

import com.skillswap.exchange_service.entity.Exchange;
import com.skillswap.exchange_service.enums.ExchangeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ExchangeRepository extends JpaRepository<Exchange, Long> {

    List<Exchange> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);

    List<Exchange> findByProviderIdOrderByCreatedAtDesc(Long providerId);

    boolean existsByRequesterIdAndSkillIdAndStatusIn(Long requesterId, Long skillId,
                                                      Collection<ExchangeStatus> statuses);
}
