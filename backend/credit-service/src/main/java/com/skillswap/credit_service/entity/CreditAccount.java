package com.skillswap.credit_service.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Compte de crédits d'un étudiant (un seul par utilisateur). */
@Entity
@Table(name = "credit_accounts")
public class CreditAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Identifiant externe (identity-service) : pas de relation JPA entre microservices
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    /** Crédits possédés. */
    @Column(nullable = false)
    private Integer balance;

    /** Part du solde bloquée pour des échanges acceptés. */
    @Column(nullable = false)
    private Integer reserved;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /** Crédits utilisables = solde - crédits bloqués. */
    public int available() {
        return balance - reserved;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Integer getBalance() { return balance; }
    public void setBalance(Integer balance) { this.balance = balance; }
    public Integer getReserved() { return reserved; }
    public void setReserved(Integer reserved) { this.reserved = reserved; }
}
