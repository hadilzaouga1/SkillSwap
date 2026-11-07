package com.SkillSwap_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfilUpdateRequest(
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 80, message = "Le nom ne doit pas dépasser 80 caractères")
        String nom,

        @Size(max = 120, message = "L'université ne doit pas dépasser 120 caractères")
        String universite,

        @Size(max = 120, message = "La filière ne doit pas dépasser 120 caractères")
        String filiere
) {
}
