package com.skillswap.skill_service.service;

import com.skillswap.skill_service.dto.CompetenceRequest;
import com.skillswap.skill_service.dto.CompetenceResponse;
import com.skillswap.skill_service.entity.Categorie;
import com.skillswap.skill_service.entity.Competence;
import com.skillswap.skill_service.entity.StatutValidation;
import com.skillswap.skill_service.exception.ApiException;
import com.skillswap.skill_service.repository.CategorieRepository;
import com.skillswap.skill_service.repository.CompetenceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompetenceService {

    private final CompetenceRepository competenceRepository;
    private final CategorieRepository categorieRepository;

    public CompetenceService(CompetenceRepository competenceRepository, CategorieRepository categorieRepository) {
        this.competenceRepository = competenceRepository;
        this.categorieRepository = categorieRepository;
    }

    public List<CompetenceResponse> rechercher(Long categorieId, String niveauStr, String motCle, Long etudiantId) {
        com.skillswap.skill_service.entity.Niveau niveau = null;
        if (niveauStr != null && !niveauStr.isBlank()) {
            niveau = com.skillswap.skill_service.entity.Niveau.valueOf(niveauStr.toUpperCase());
        }
        String motCleSafe = (motCle == null) ? "" : motCle;

        return competenceRepository.rechercher(StatutValidation.VALIDEE, categorieId, niveau, etudiantId, motCleSafe)
                .stream().map(CompetenceResponse::depuis).toList();
    }

    public List<CompetenceResponse> getMesCompetences(Long etudiantId) {
        return competenceRepository.findByEtudiantIdOrderByIdDesc(etudiantId)
                .stream().map(CompetenceResponse::depuis).toList();
    }

    public CompetenceResponse getParId(Long id) {
        Competence c = competenceRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Compétence introuvable"));
        return CompetenceResponse.depuis(c);
    }

    public CompetenceResponse creer(CompetenceRequest request, Long etudiantId) {
        Categorie categorie = categorieRepository.findById(request.categorieId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Catégorie introuvable"));

        Competence c = new Competence();
        c.setTitre(request.titre());
        c.setDescription(request.description());
        c.setNiveau(request.niveau());
        c.setDisponibilites(request.disponibilites());
        c.setCategorie(categorie);
        c.setEtudiantId(etudiantId);
        // Toute nouvelle compétence attend la validation d'un modérateur (US20)
        c.setStatutValidation(StatutValidation.EN_ATTENTE);

        return CompetenceResponse.depuis(competenceRepository.save(c));
    }

    public CompetenceResponse modifier(Long id, CompetenceRequest request, Long etudiantId) {
        Competence c = competenceRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Compétence introuvable"));

        if (!c.getEtudiantId().equals(etudiantId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Vous ne pouvez pas modifier cette compétence");
        }

        Categorie categorie = categorieRepository.findById(request.categorieId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Catégorie introuvable"));

        c.setTitre(request.titre());
        c.setDescription(request.description());
        c.setNiveau(request.niveau());
        c.setDisponibilites(request.disponibilites());
        c.setCategorie(categorie);
        // Une compétence modifiée doit être revalidée (sinon on pourrait modifier après validation)
        c.setStatutValidation(StatutValidation.EN_ATTENTE);

        return CompetenceResponse.depuis(competenceRepository.save(c));
    }

    public void supprimer(Long id, Long etudiantId) {
        Competence c = competenceRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Compétence introuvable"));

        if (!c.getEtudiantId().equals(etudiantId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Vous ne pouvez pas supprimer cette compétence");
        }

        competenceRepository.delete(c);
    }
}
