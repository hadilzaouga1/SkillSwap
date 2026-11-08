package com.skillswap.exchange_service.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Vue minimale de CompetenceResponse (skill-service) : seuls les champs utiles ici.
@JsonIgnoreProperties(ignoreUnknown = true)
public record SkillDto(
        Long id,
        String titre,
        String statutValidation,   // EN_ATTENTE, VALIDEE ou REFUSEE
        Long etudiantId            // le prestataire propriétaire de la compétence
) {}
