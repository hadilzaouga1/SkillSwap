package com.SkillSwap_api.dto;

import jakarta.validation.constraints.NotBlank;

public record SyncRequest(@NotBlank String keycloakId,
                          @NotBlank String email,
                          String nom,
                          boolean emailVerified) {
    
}
