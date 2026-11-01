package com.SkillSwap_api.service;

import com.SkillSwap_api.dto.CompetenceRequest;
import com.SkillSwap_api.dto.CompetenceResponse;
import com.SkillSwap_api.entity.Categorie;
import com.SkillSwap_api.entity.Competence;
import com.SkillSwap_api.entity.Niveau;
import com.SkillSwap_api.entity.StatutValidation;
import com.SkillSwap_api.entity.Utilisateur;
import com.SkillSwap_api.exception.ApiException;
import com.SkillSwap_api.repository.CategorieRepository;
import com.SkillSwap_api.repository.CompetenceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Logique métier des compétences
 */
@Service
public class CompetenceService {
    private static final StatutValidation STATUT_INITIAL = StatutValidation.VALIDEE;

    private final CompetenceRepository competenceRepository;
    private final CategorieRepository categorieRepository;
    private final UtilisateurCourantService utilisateurCourantService;

    public CompetenceService(CompetenceRepository competenceRepository,
                             CategorieRepository categorieRepository,
                             UtilisateurCourantService utilisateurCourantService) {
        this.competenceRepository = competenceRepository;
        this.categorieRepository = categorieRepository;
        this.utilisateurCourantService = utilisateurCourantService;
    }

    @Transactional(readOnly = true)
    public List<CompetenceResponse> rechercher(Long categorieId, Niveau niveau, String motCle, Long etudiantId) {
        String mot = (motCle == null) ? "" : motCle.trim();
        return competenceRepository
                .rechercher(StatutValidation.VALIDEE, categorieId, niveau, etudiantId, mot)
                /** .stream():permet de parcourir et traiter les éléments d'une collection*/
                .stream()  
                // Transformer chaque entité Competence en un objet CompetenceResponse grâce à la méthode depuis().
                .map(CompetenceResponse::depuis)
                .toList();
    }

    @Transactional(readOnly = true)
    public CompetenceResponse getParId(Long id) {
        return CompetenceResponse.depuis(trouver(id));
    }

    @Transactional(readOnly = true)
    public List<CompetenceResponse> getMesCompetences() {
        Utilisateur moi = utilisateurCourantService.getUtilisateurCourant();
        return competenceRepository.findByEtudiantIdOrderByIdDesc(moi.getId())
                .stream()
                .map(CompetenceResponse::depuis)
                .toList();
    }

    @Transactional
    public CompetenceResponse creer(CompetenceRequest request) {
        Utilisateur moi = utilisateurCourantService.getUtilisateurCourant();

        Competence competence = new Competence();
        competence.setEtudiant(moi);
        competence.setStatutValidation(STATUT_INITIAL);
        appliquer(competence, request);

        return CompetenceResponse.depuis(competenceRepository.save(competence));
    }

    @Transactional
    public CompetenceResponse modifier(Long id, CompetenceRequest request) {
        Competence competence = trouver(id);
        verifierProprietaire(competence);
        appliquer(competence, request);
        return CompetenceResponse.depuis(competenceRepository.save(competence));
    }

    @Transactional
    public void supprimer(Long id) {
        Competence competence = trouver(id);
        verifierProprietaire(competence);
        competenceRepository.delete(competence);
    }


    private Competence trouver(Long id) {
        return competenceRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Compétence introuvable"));
    }

    private void verifierProprietaire(Competence competence) {
        Utilisateur moi = utilisateurCourantService.getUtilisateurCourant();
        if (!competence.getEtudiant().getId().equals(moi.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Vous ne pouvez modifier que vos propres compétences");
        }
    }

    //remplir ou mettre à jour les informations d'une compétence à partir des données reçues dans CompetenceRequest
    private void appliquer(Competence competence, CompetenceRequest request) {
        Categorie categorie = categorieRepository.findById(request.categorieId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Catégorie introuvable"));

        competence.setTitre(request.titre().trim());
        competence.setDescription(request.description());
        competence.setNiveau(request.niveau());
        competence.setDisponibilites(request.disponibilites());
        competence.setCategorie(categorie);
    }
}
