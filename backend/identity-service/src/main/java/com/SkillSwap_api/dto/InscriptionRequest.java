package com.SkillSwap_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Données du formulaire d'inscription (US1). */
public record InscriptionRequest(
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 80, message = "Le nom ne doit pas dépasser 80 caractères")
        String nom,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Format d'email invalide")
        @Size(max = 120, message = "L'email ne doit pas dépasser 120 caractères")
        String email,

        // 72 = limite de BCrypt (octets pris en compte)
        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, max = 72, message = "Le mot de passe doit contenir entre 8 et 72 caractères")
        String motDePasse,

        @Size(max = 120, message = "L'université ne doit pas dépasser 120 caractères")
        String universite,

        @Size(max = 120, message = "La filière ne doit pas dépasser 120 caractères")
        String filiere
) {
}

