package com.SkillSwap_api.controller;

import com.SkillSwap_api.dto.ProfilPublicResponse;
import com.SkillSwap_api.dto.ProfilResponse;
import com.SkillSwap_api.dto.ProfilUpdateRequest;
import com.SkillSwap_api.service.UtilisateurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/** API REST des profils : /api/utilisateurs */
@RestController
@RequestMapping("/api/utilisateurs")
@Tag(name = "Utilisateurs / Profils")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @Operation(summary = "Mon profil (avec email)")
    @GetMapping("/me")
    public ProfilResponse monProfil() {
        return utilisateurService.getMonProfil();
    }

    @Operation(summary = "Modifier mon profil (US3)")
    @PutMapping("/me")
    public ProfilResponse modifierMonProfil(@Valid @RequestBody ProfilUpdateRequest request) {
        return utilisateurService.modifierMonProfil(request);
    }

    @Operation(summary = "Consulter le profil d'un étudiant (US8)")
    @GetMapping("/{id}")
    public ProfilPublicResponse profilPublic(@PathVariable("id") Long id) {
        return utilisateurService.getProfilPublic(id);
    }
}
