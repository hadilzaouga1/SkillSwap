package com.skillswap.credit_service.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/** Ligne de l'historique des crédits d'un utilisateur. Jamais modifiée ni supprimée. */
@Entity
@Table(name = "credit_transactions")
public class CreditTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "exchange_id")
    private Long exchangeId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    /** Toujours positif : le sens est donné par le type. */
    @Column(nullable = false)
    private Integer amount;

    /** Solde total après l'opération. */
    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    protected CreditTransaction() {
    }

    public CreditTransaction(Long userId, Long exchangeId, TransactionType type,
                             int amount, int balanceAfter, String description) {
        this.userId = userId;
        this.exchangeId = exchangeId;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getExchangeId() { return exchangeId; }
    public TransactionType getType() { return type; }
    public Integer getAmount() { return amount; }
    public Integer getBalanceAfter() { return balanceAfter; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
