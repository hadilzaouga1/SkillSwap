package com.skillswap.skill_service.dto;

import com.skillswap.skill_service.entity.Niveau;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CompetenceRequest(
        @NotBlank(message = "Le titre est obligatoire")
        @Size(max = 120, message = "Le titre ne doit pas dépasser 120 caractères")
        String titre,

        @Size(max = 2000, message = "La description ne doit pas dépasser 2000 caractères")
        String description,

        @NotNull(message = "Le niveau est obligatoire")
        Niveau niveau,

        @NotNull(message = "La catégorie est obligatoire")
        Long categorieId,

        @Size(max = 255, message = "Les disponibilités ne doivent pas dépasser 255 caractères")
        String disponibilites
) {}
