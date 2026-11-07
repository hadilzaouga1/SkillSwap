package com.skillswap.skill_service.controller;


import com.skillswap.skill_service.dto.CompetenceRequest;
import com.skillswap.skill_service.dto.CompetenceResponse;
import com.skillswap.skill_service.service.CompetenceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/competences")
public class CompetenceController {

    private final CompetenceService competenceService;

    public CompetenceController(CompetenceService competenceService) {
        this.competenceService = competenceService;
    }

    private Long getUserId(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new RuntimeException("Header X-User-Id manquant");
        }
        return Long.parseLong(userIdHeader);
    }

    @GetMapping
    public List<CompetenceResponse> rechercher(
            @RequestParam(name = "categorieId", required = false) Long categorieId,
            @RequestParam(name = "niveau", required = false) String niveau,
            @RequestParam(name = "motCle", required = false) String motCle,
            @RequestParam(name = "etudiantId", required = false) Long etudiantId) {
        return competenceService.rechercher(categorieId, niveau, motCle, etudiantId);
    }

    @GetMapping("/mes-competences")
    public List<CompetenceResponse> mesCompetences(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        return competenceService.getMesCompetences(getUserId(userIdHeader));
    }

    @GetMapping("/{id}")
    public CompetenceResponse getParId(@PathVariable("id") Long id) {
        return competenceService.getParId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompetenceResponse creer(
            @Valid @RequestBody CompetenceRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        return competenceService.creer(request, getUserId(userIdHeader));
    }

    @PutMapping("/{id}")
    public CompetenceResponse modifier(
            @PathVariable("id") Long id,
            @Valid @RequestBody CompetenceRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        return competenceService.modifier(id, request, getUserId(userIdHeader));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(
            @PathVariable("id") Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        competenceService.supprimer(id, getUserId(userIdHeader));
    }
}
