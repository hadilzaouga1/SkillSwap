package com.skillswap.credit_service.entity;

/** Nature d'un mouvement dans l'historique d'un utilisateur (US14). */
public enum TransactionType {
    WELCOME,       // crédits de bienvenue
    RESERVATION,   // crédits bloqués pour un échange accepté
    RELEASE,       // crédits débloqués (échange annulé)
    TRANSFER_OUT,  // crédits payés au prestataire
    TRANSFER_IN    // crédits reçus pour une séance donnée
}
