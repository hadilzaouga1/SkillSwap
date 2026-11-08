package com.skillswap.exchange_service.entity;

import com.skillswap.exchange_service.enums.ExchangeStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "exchanges")
@Getter
@Setter
@NoArgsConstructor
public class Exchange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Identifiants externes (identity-service) : pas de relation JPA entre microservices
    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    // Identifiant externe (skill-service)
    @Column(name = "skill_id", nullable = false)
    private Long skillId;

    // Copie du titre au moment de la demande, pour l'affichage sans rappeler skill-service
    @Column(name = "skill_title", nullable = false, length = 255)
    private String skillTitle;

    @Column(length = 1000)
    private String description;

    @Column(name = "duration_hours", nullable = false)
    private Integer durationHours;

    @Column(name = "proposed_date_time", nullable = false)
    private LocalDateTime proposedDateTime;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private ExchangeStatus status;

    // Verrou optimiste : empêche deux acceptations/confirmations simultanées
    @Version
    @Column(nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = ExchangeStatus.PENDING;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
