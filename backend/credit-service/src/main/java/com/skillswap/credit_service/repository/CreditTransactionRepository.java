package com.skillswap.credit_service.repository;

import com.skillswap.credit_service.entity.CreditTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, Long> {

    /** Les 100 derniers mouvements d'un utilisateur, du plus récent au plus ancien. */
    List<CreditTransaction> findTop100ByUserIdOrderByIdDesc(Long userId);
}
