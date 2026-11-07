package com.SkillSwap_api.dto;

import jakarta.validation.constraints.NotBlank;

/** Données du formulaire de connexion (US2). */
public record ConnexionRequest(
        @NotBlank(message = "L'email est obligatoire")
        String email,

        @NotBlank(message = "Le mot de passe est obligatoire")
        String motDePasse
) {
}
