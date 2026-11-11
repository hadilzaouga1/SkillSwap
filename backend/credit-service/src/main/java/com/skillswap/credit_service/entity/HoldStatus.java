package com.skillswap.credit_service.entity;

/** État d'une réservation de crédits liée à un échange. */
public enum HoldStatus {
    /** Crédits bloqués chez le demandeur (échange accepté). */
    HELD,
    /** Crédits versés au prestataire (échange confirmé). */
    TRANSFERRED,
    /** Crédits rendus au demandeur (échange annulé). */
    RELEASED
}
