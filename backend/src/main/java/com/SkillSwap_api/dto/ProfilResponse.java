package com.SkillSwap_api.dto;

import com.SkillSwap_api.entity.Utilisateur;

/** Profil complet, visible uniquement par son propriétaire (GET /api/utilisateurs/me). */
public record ProfilResponse(
        Long id,
        String nom,
        String email,
        String universite,
        String filiere,
        Double noteMoyenne
) {
    public static ProfilResponse depuis(Utilisateur u) {
        return new ProfilResponse(u.getId(), u.getNom(), u.getEmail(),
                u.getUniversite(), u.getFiliere(), u.getNoteMoyenne());
    }
}
