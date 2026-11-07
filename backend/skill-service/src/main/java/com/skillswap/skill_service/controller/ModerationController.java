package com.skillswap.skill_service.controller;

import com.skillswap.skill_service.dto.CompetenceResponse;
import com.skillswap.skill_service.entity.StatutValidation;
import com.skillswap.skill_service.service.ModerationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Routes de modération : /api/moderation/competences
 * (protégées par la Gateway : rôle MODERATEUR ou ADMIN uniquement).
 */
@RestController
@RequestMapping("/api/moderation/competences")
public class ModerationController {

    private final ModerationService moderationService;

    public ModerationController(ModerationService moderationService) {
        this.moderationService = moderationService;
    }

    /** Liste des compétences à valider (par défaut EN_ATTENTE). */
    @GetMapping
    public List<CompetenceResponse> lister(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestParam(name = "statut", required = false, defaultValue = "EN_ATTENTE") StatutValidation statut) {
        return moderationService.lister(statut, role);
    }

    @PutMapping("/{id}/valider")
    public CompetenceResponse valider(
            @PathVariable("id") Long id,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        return moderationService.valider(id, role);
    }

    @PutMapping("/{id}/refuser")
    public CompetenceResponse refuser(
            @PathVariable("id") Long id,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        return moderationService.refuser(id, role);
    }
}
