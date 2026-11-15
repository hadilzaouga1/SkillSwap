package com.skillswap.skill_service.service;

import com.skillswap.skill_service.dto.CompetenceRequest;
import com.skillswap.skill_service.dto.CompetenceResponse;
import com.skillswap.skill_service.entity.Categorie;
import com.skillswap.skill_service.entity.Competence;
import com.skillswap.skill_service.entity.Niveau;
import com.skillswap.skill_service.entity.StatutValidation;
import com.skillswap.skill_service.exception.ApiException;
import com.skillswap.skill_service.repository.CategorieRepository;
import com.skillswap.skill_service.repository.CompetenceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class CompetenceService {

    private final CompetenceRepository competenceRepository;
    private final CategorieRepository categorieRepository;
    private final String uploadDir = "data/uploads/competences";

    public CompetenceService(CompetenceRepository competenceRepository, CategorieRepository categorieRepository) {
        this.competenceRepository = competenceRepository;
        this.categorieRepository = categorieRepository;
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public List<CompetenceResponse> rechercher(Long categorieId, String niveauStr, String motCle, Long etudiantId) {
        Niveau niveau = null;
        if (niveauStr != null && !niveauStr.isBlank()) {
            niveau = Niveau.valueOf(niveauStr.toUpperCase());
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

    public CompetenceResponse creer(CompetenceRequest request, MultipartFile file, Long etudiantId) {
        Categorie categorie = categorieRepository.findById(request.categorieId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Catégorie introuvable"));

        Competence c = new Competence();
        c.setTitre(request.titre());
        c.setDescription(request.description());
        c.setNiveau(request.niveau());
        c.setDisponibilites(request.disponibilites());
        c.setCategorie(categorie);
        c.setEtudiantId(etudiantId);
        c.setStatutValidation(StatutValidation.EN_ATTENTE);

        traiterFichier(c, file);

        return CompetenceResponse.depuis(competenceRepository.save(c));
    }

    public CompetenceResponse modifier(Long id, CompetenceRequest request, MultipartFile file, Long etudiantId) {
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
        c.setStatutValidation(StatutValidation.EN_ATTENTE);

        if (file != null && !file.isEmpty()) {
            supprimerFichierPrecedent(c);
            traiterFichier(c, file);
        }

        return CompetenceResponse.depuis(competenceRepository.save(c));
    }

    public void supprimer(Long id, Long etudiantId) {
        Competence c = competenceRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Compétence introuvable"));

        if (!c.getEtudiantId().equals(etudiantId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Vous ne pouvez pas supprimer cette compétence");
        }

        supprimerFichierPrecedent(c);
        competenceRepository.delete(c);
    }

    private void traiterFichier(Competence c, MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            String originalName = file.getOriginalFilename();
            boolean isPdfByContentType = "application/pdf".equals(file.getContentType());
            boolean isPdfByExtension = originalName != null
                    && originalName.toLowerCase().endsWith(".pdf");

            if (!isPdfByContentType && !isPdfByExtension) {
                throw new ApiException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Seuls les fichiers PDF sont acceptés");
            }
            if (file.getSize() > 10 * 1024 * 1024) {
                throw new ApiException(HttpStatus.PAYLOAD_TOO_LARGE, "Le fichier dépasse 10 Mo");
            }
            try {
                String storedName = UUID.randomUUID().toString() + ".pdf";
                Path path = Paths.get(uploadDir, storedName);
                Files.write(path, file.getBytes());

                c.setDocumentName(originalName);
                c.setStoredName(storedName);
                c.setDocumentSize(file.getSize());
                c.setDocumentUrl("/api/competences/files/" + storedName);
            } catch (IOException e) {
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de l'enregistrement du fichier");
            }
        }
    }

    private void supprimerFichierPrecedent(Competence c) {
        if (c.getStoredName() != null) {
            try {
                Files.deleteIfExists(Paths.get(uploadDir, c.getStoredName()));
            } catch (IOException ignored) {}
        }
    }
}