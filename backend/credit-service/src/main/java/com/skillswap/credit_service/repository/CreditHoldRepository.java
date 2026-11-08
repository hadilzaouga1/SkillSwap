package com.skillswap.credit_service.repository;

import com.skillswap.credit_service.entity.CreditHold;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CreditHoldRepository extends JpaRepository<CreditHold, Long> {

    /** Réservation d'un échange, verrouillée (évite un double transfert ou un double déblocage). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CreditHold> findWithLockByExchangeId(Long exchangeId);
}
