package com.skillswap.credit_service.repository;

import com.skillswap.credit_service.entity.CreditAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CreditAccountRepository extends JpaRepository<CreditAccount, Long> {

    /** Lecture simple (affichage du solde). */
    Optional<CreditAccount> findByUserId(Long userId);

    /**
     * Lecture AVEC verrou (SELECT ... FOR UPDATE) : tant que la transaction n'est pas
     * terminée, personne d'autre ne peut modifier ce compte. Indispensable pour qu'un solde
     * ne soit jamais dépensé deux fois en parallèle (ENF2).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CreditAccount> findWithLockByUserId(Long userId);

    /**
     * Crée le compte s'il n'existe pas (INSERT IGNORE de MySQL, sûr même si deux requêtes
     * arrivent en même temps). Renvoie 1 si le compte vient d'être créé, 0 s'il existait déjà.
     */
    @Modifying
    @Query(value = "INSERT IGNORE INTO credit_accounts (user_id, balance, reserved, created_at, updated_at) "
            + "VALUES (:userId, :balance, 0, NOW(6), NOW(6))", nativeQuery = true)
    int insertIfAbsent(@Param("userId") Long userId, @Param("balance") int balance);
}
