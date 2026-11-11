package com.SkillSwap_api.dto;

import com.SkillSwap_api.entity.Role;
import com.SkillSwap_api.entity.Utilisateur;

/**
 * Réponse à l'inscription et à la connexion :
 * le token JWT à renvoyer ensuite dans "Authorization: Bearer ..." + les infos de l'utilisateur.
 */
public record AuthResponse(String token, UtilisateurConnecte utilisateur) {

    /** Informations non sensibles (jamais le mot de passe). */
    public record UtilisateurConnecte(Long id, String nom, String email, Role role) {
    }

    public static AuthResponse de(String token, Utilisateur u) {
        return new AuthResponse(token,
                new UtilisateurConnecte(u.getId(), u.getNom(), u.getEmail(), u.getRole()));
    }
}
