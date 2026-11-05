package com.skillswap.skill_service.dto;

import com.skillswap.skill_service.entity.Competence;
import com.skillswap.skill_service.entity.Niveau;
import com.skillswap.skill_service.entity.StatutValidation;

public record CompetenceResponse(
        Long id,
        String titre,
        String description,
        Niveau niveau,
        String disponibilites,
        StatutValidation statutValidation,
        CategorieInfo categorie,
        Long etudiantId
) {
    public record CategorieInfo(Long id, String nom) {}

    public static CompetenceResponse depuis(Competence c) {
        return new CompetenceResponse(
                c.getId(),
                c.getTitre(),
                c.getDescription(),
                c.getNiveau(),
                c.getDisponibilites(),
                c.getStatutValidation(),
                new CategorieInfo(c.getCategorie().getId(), c.getCategorie().getNom()),
                c.getEtudiantId()
        );
    }
}
