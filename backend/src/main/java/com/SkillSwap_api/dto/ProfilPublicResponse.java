package com.SkillSwap_api.dto;

import com.SkillSwap_api.entity.Utilisateur;

public record ProfilPublicResponse(
        Long id,
        String nom,
        String universite,
        String filiere,
        Double noteMoyenne
) {
    public static ProfilPublicResponse depuis(Utilisateur u) {
        return new ProfilPublicResponse(u.getId(), u.getNom(),
                u.getUniversite(), u.getFiliere(), u.getNoteMoyenne());
    }
}
