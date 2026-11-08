package com.skillswap.exchange_service.enums;

public enum ExchangeStatus {
    PENDING,                // demande envoyée, en attente de réponse du prestataire
    TIME_PROPOSED,          // le prestataire a proposé un autre créneau, le demandeur doit répondre
    ACCEPTED,               // demande acceptée, séance à réaliser
    AWAITING_CONFIRMATION,  // le prestataire a déclaré la séance terminée
    COMPLETED,              // le demandeur a confirmé : échange terminé
    REJECTED,               // refusée par le prestataire
    CANCELLED               // annulée par l'un des participants
}
