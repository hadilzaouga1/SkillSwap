package com.skillswap.credit_service.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/** Réservation de crédits pour un échange (une seule par échange). */
@Entity
@Table(name = "credit_holds")
public class CreditHold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Identifiant externe (exchange-service)
    @Column(name = "exchange_id", nullable = false, unique = true)
    private Long exchangeId;

    /** Celui qui paie (le demandeur). */
    @Column(name = "payer_id", nullable = false)
    private Long payerId;

    /** Celui qui reçoit (le prestataire). */
    @Column(name = "payee_id", nullable = false)
    private Long payeeId;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private HoldStatus status;

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

    public Long getId() { return id; }
    public Long getExchangeId() { return exchangeId; }
    public void setExchangeId(Long exchangeId) { this.exchangeId = exchangeId; }
    public Long getPayerId() { return payerId; }
    public void setPayerId(Long payerId) { this.payerId = payerId; }
    public Long getPayeeId() { return payeeId; }
    public void setPayeeId(Long payeeId) { this.payeeId = payeeId; }
    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }
    public HoldStatus getStatus() { return status; }
    public void setStatus(HoldStatus status) { this.status = status; }
}
