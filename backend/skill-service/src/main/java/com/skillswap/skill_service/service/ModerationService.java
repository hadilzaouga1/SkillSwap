package com.skillswap.skill_service.service;

import com.skillswap.skill_service.dto.CompetenceResponse;
import com.skillswap.skill_service.entity.Competence;
import com.skillswap.skill_service.entity.StatutValidation;
import com.skillswap.skill_service.exception.ApiException;
import com.skillswap.skill_service.repository.CompetenceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Validation des compétences par un modérateur (US20).
 *
 * Le rôle vient de l'en-tête X-User-Role, fourni par l'API Gateway APRÈS vérification
 * du token JWT (le client ne peut pas le falsifier : la Gateway supprime ses propres
 * en-têtes X-User-*). Le contrôle est répété ici par sécurité (défense en profondeur).
 */
@Service
public class ModerationService {

    private final CompetenceRepository competenceRepository;

    public ModerationService(CompetenceRepository competenceRepository) {
        this.competenceRepository = competenceRepository;
    }

    @Transactional(readOnly = true)
    public List<CompetenceResponse> lister(StatutValidation statut, String role) {
        verifierModerateur(role);
        return competenceRepository.findByStatutValidationOrderByIdAsc(statut)
                .stream().map(CompetenceResponse::depuis).toList();
    }

    @Transactional
    public CompetenceResponse valider(Long id, String role) {
        verifierModerateur(role);
        return changerStatut(id, StatutValidation.VALIDEE);
    }

    @Transactional
    public CompetenceResponse refuser(Long id, String role) {
        verifierModerateur(role);
        return changerStatut(id, StatutValidation.REFUSEE);
    }

    private CompetenceResponse changerStatut(Long id, StatutValidation nouveauStatut) {
        Competence c = competenceRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Compétence introuvable"));
        c.setStatutValidation(nouveauStatut);
        return CompetenceResponse.depuis(competenceRepository.save(c));
    }

    /** Seuls MODERATEUR (et ADMIN, pour dépanner) peuvent modérer. */
    private void verifierModerateur(String role) {
        boolean autorise = "MODERATEUR".equals(role) || "ADMIN".equals(role);
        if (!autorise) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Réservé aux modérateurs");
        }
    }
}
