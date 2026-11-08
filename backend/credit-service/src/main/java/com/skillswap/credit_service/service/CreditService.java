package com.skillswap.credit_service.service;

import com.skillswap.credit_service.dto.BalanceResponse;
import com.skillswap.credit_service.dto.HoldRequest;
import com.skillswap.credit_service.dto.HoldResponse;
import com.skillswap.credit_service.dto.TransactionResponse;
import com.skillswap.credit_service.entity.*;
import com.skillswap.credit_service.exception.ApiException;
import com.skillswap.credit_service.repository.CreditAccountRepository;
import com.skillswap.credit_service.repository.CreditHoldRepository;
import com.skillswap.credit_service.repository.CreditTransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ============================================================
 *  COEUR DE LA RÈGLE ENF2 : "aucun crédit perdu ni créé"
 * ============================================================
 * Cycle de vie des crédits d'un échange :
 *
 *   échange ACCEPTÉ   -> reserve()  : bloque les crédits du demandeur  (reserved += montant)
 *   échange ANNULÉ    -> release()  : débloque (reserved -= montant)
 *   échange CONFIRMÉ  -> transfer() : demandeur -montant, prestataire +montant
 *
 * Garanties :
 *  - chaque méthode est UNE transaction : tout réussit ou tout est annulé ;
 *  - les comptes sont verrouillés (FOR UPDATE) : pas de double dépense en parallèle ;
 *  - reserve / release / transfer sont IDEMPOTENTES : si exchange-service renvoie la même
 *    demande après une panne réseau, rien n'est compté deux fois ;
 *  - les CHECK de la base interdisent un solde négatif en dernier recours.
 *
 * Règle d'ordre des verrous (évite les blocages mutuels) :
 *  réservation d'échange d'abord, puis comptes par ordre croissant d'id utilisateur.
 */
@Service
public class CreditService {

    private final CreditAccountRepository accountRepository;
    private final CreditHoldRepository holdRepository;
    private final CreditTransactionRepository transactionRepository;
    private final int welcomeCredits;

    public CreditService(CreditAccountRepository accountRepository,
                         CreditHoldRepository holdRepository,
                         CreditTransactionRepository transactionRepository,
                         @Value("${skillswap.credits.welcome:3}") int welcomeCredits) {
        this.accountRepository = accountRepository;
        this.holdRepository = holdRepository;
        this.transactionRepository = transactionRepository;
        this.welcomeCredits = welcomeCredits;
    }

    // ------------------------------------------------------------ consultation

    /** Solde d'un utilisateur (le compte est créé avec les crédits de bienvenue au premier accès). */
    @Transactional
    public BalanceResponse getBalance(Long userId) {
        createAccountIfAbsent(userId);
        CreditAccount account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Compte introuvable"));
        return BalanceResponse.from(account);
    }

    /** Historique (US14) : les 100 derniers mouvements. */
    @Transactional
    public List<TransactionResponse> getTransactions(Long userId) {
        createAccountIfAbsent(userId); // pour que le crédit de bienvenue apparaisse
        return transactionRepository.findTop100ByUserIdOrderByIdDesc(userId)
                .stream().map(TransactionResponse::from).toList();
    }

    // ------------------------------------------------------------ opérations

    /** Bloque les crédits du demandeur quand l'échange est accepté. */
    @Transactional
    public HoldResponse reserve(HoldRequest request) {
        if (request.payerId().equals(request.payeeId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Le demandeur et le prestataire doivent être différents");
        }

        // 1. On verrouille d'abord le compte du demandeur : les réservations d'un même
        //    demandeur sont ainsi traitées l'une après l'autre.
        CreditAccount payer = lockedAccount(request.payerId());

        // 2. Idempotence : cet échange a-t-il déjà une réservation ?
        CreditHold existing = holdRepository.findWithLockByExchangeId(request.exchangeId()).orElse(null);
        if (existing != null) {
            if (existing.getStatus() == HoldStatus.HELD) {
                return HoldResponse.from(existing); // déjà réservé : on ne compte pas deux fois
            }
            throw new ApiException(HttpStatus.CONFLICT,
                    "Les crédits de cet échange ont déjà été " + libelle(existing.getStatus()));
        }

        // 3. Le demandeur a-t-il assez de crédits DISPONIBLES ?
        if (payer.available() < request.amount()) {
            throw new ApiException(HttpStatus.CONFLICT, "Solde insuffisant : " + payer.available()
                    + " crédit(s) disponible(s), " + request.amount() + " nécessaire(s)");
        }

        payer.setReserved(payer.getReserved() + request.amount());

        CreditHold hold = new CreditHold();
        hold.setExchangeId(request.exchangeId());
        hold.setPayerId(request.payerId());
        hold.setPayeeId(request.payeeId());
        hold.setAmount(request.amount());
        hold.setStatus(HoldStatus.HELD);
        holdRepository.save(hold);

        log(payer.getUserId(), request.exchangeId(), TransactionType.RESERVATION,
                request.amount(), payer.getBalance(),
                "Crédits bloqués pour l'échange n°" + request.exchangeId());

        return HoldResponse.from(hold);
    }

    /**
     * Débloque les crédits (échange annulé). Sans effet s'il n'y a rien à débloquer
     * (échange jamais accepté) ou si c'est déjà fait : exchange-service peut l'appeler sans risque.
     */
    @Transactional
    public void release(Long exchangeId) {
        CreditHold hold = holdRepository.findWithLockByExchangeId(exchangeId).orElse(null);
        if (hold == null || hold.getStatus() == HoldStatus.RELEASED) {
            return;
        }
        if (hold.getStatus() == HoldStatus.TRANSFERRED) {
            throw new ApiException(HttpStatus.CONFLICT, "Les crédits de cet échange ont déjà été transférés");
        }

        CreditAccount payer = lockedAccount(hold.getPayerId());
        payer.setReserved(payer.getReserved() - hold.getAmount());
        hold.setStatus(HoldStatus.RELEASED);

        log(payer.getUserId(), exchangeId, TransactionType.RELEASE,
                hold.getAmount(), payer.getBalance(),
                "Crédits débloqués (échange n°" + exchangeId + " annulé)");
    }

    /** Verse les crédits bloqués au prestataire (échange confirmé). */
    @Transactional
    public HoldResponse transfer(Long exchangeId) {
        CreditHold hold = holdRepository.findWithLockByExchangeId(exchangeId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "Aucune réservation de crédits pour cet échange"));

        if (hold.getStatus() == HoldStatus.TRANSFERRED) {
            return HoldResponse.from(hold); // déjà fait : idempotent
        }
        if (hold.getStatus() == HoldStatus.RELEASED) {
            throw new ApiException(HttpStatus.CONFLICT, "Les crédits de cet échange ont été débloqués");
        }

        // Verrou des DEUX comptes, toujours dans le même ordre (id utilisateur croissant)
        Long first = Math.min(hold.getPayerId(), hold.getPayeeId());
        Long second = Math.max(hold.getPayerId(), hold.getPayeeId());
        CreditAccount firstAccount = lockedAccount(first);
        CreditAccount secondAccount = lockedAccount(second);
        CreditAccount payer = first.equals(hold.getPayerId()) ? firstAccount : secondAccount;
        CreditAccount payee = first.equals(hold.getPayerId()) ? secondAccount : firstAccount;

        int amount = hold.getAmount();
        payer.setBalance(payer.getBalance() - amount);
        payer.setReserved(payer.getReserved() - amount);
        payee.setBalance(payee.getBalance() + amount);
        hold.setStatus(HoldStatus.TRANSFERRED);

        log(payer.getUserId(), exchangeId, TransactionType.TRANSFER_OUT, amount, payer.getBalance(),
                "Séance payée (échange n°" + exchangeId + ")");
        log(payee.getUserId(), exchangeId, TransactionType.TRANSFER_IN, amount, payee.getBalance(),
                "Séance réalisée (échange n°" + exchangeId + ")");

        return HoldResponse.from(hold);
    }

    // ------------------------------------------------------------ méthodes privées

    /** Crée le compte s'il n'existe pas, avec les crédits de bienvenue. */
    private void createAccountIfAbsent(Long userId) {
        if (accountRepository.insertIfAbsent(userId, welcomeCredits) == 1) {
            log(userId, null, TransactionType.WELCOME, welcomeCredits, welcomeCredits,
                    "Crédits de bienvenue");
        }
    }

    /** Compte verrouillé jusqu'à la fin de la transaction (créé au besoin). */
    private CreditAccount lockedAccount(Long userId) {
        createAccountIfAbsent(userId);
        return accountRepository.findWithLockByUserId(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Compte introuvable"));
    }

    private void log(Long userId, Long exchangeId, TransactionType type,
                     int amount, int balanceAfter, String description) {
        transactionRepository.save(new CreditTransaction(userId, exchangeId, type, amount, balanceAfter, description));
    }

    private String libelle(HoldStatus status) {
        return status == HoldStatus.TRANSFERRED ? "transférés" : "débloqués";
    }
}
