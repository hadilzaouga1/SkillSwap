package com.SkillSwap_api.dto;

public record AdminStatsResponse(
        long totalEtudiants,
        long totalCompetences,
        long enAttente,
        long echangesEnCours
) {}