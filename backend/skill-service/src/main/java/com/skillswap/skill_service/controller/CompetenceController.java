package com.skillswap.skill_service.controller;

import com.skillswap.skill_service.dto.CompetenceRequest;
import com.skillswap.skill_service.dto.CompetenceResponse;
import com.skillswap.skill_service.exception.ApiException;
import com.skillswap.skill_service.service.CompetenceService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * API REST des compétences : /api/competences
 *
 * L'identité de l'utilisateur vient UNIQUEMENT de l'en-tête X-User-Id, posé par l'API Gateway
 * après vérification du token JWT.
 *
 * Création et modification existent en deux versions :
 *  - multipart : partie "competence" (JSON) + partie "file" (PDF facultatif) ;
 *  - JSON simple, sans document (le formulaire Angular l'utilise quand aucun PDF n'est joint).
 */
@RestController
@RequestMapping("/api/competences")
public class CompetenceController {

    private final CompetenceService competenceService;

    // Dossier où le CompetenceService enregistre les PDF (doit rester identique à celui du service)
    private final Path fileStorageLocation = Paths.get("data/uploads/competences").toAbsolutePath().normalize();

    public CompetenceController(CompetenceService competenceService) {
        this.competenceService = competenceService;
    }

    private Long getUserId(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentification requise");
        }
        try {
            return Long.parseLong(userIdHeader);
        } catch (NumberFormatException ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Identité invalide");
        }
    }

    // ------------------------------------------------------------ lecture

    @GetMapping
    public List<CompetenceResponse> rechercher(
            @RequestParam(name = "categorieId", required = false) Long categorieId,
            @RequestParam(name = "niveau", required = false) String niveau,
            @RequestParam(name = "motCle", required = false) String motCle,
            @RequestParam(name = "etudiantId", required = false) Long etudiantId) {
        return competenceService.rechercher(categorieId, niveau, motCle, etudiantId);
    }

    @GetMapping("/mes-competences")
    public List<CompetenceResponse> mesCompetences(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        return competenceService.getMesCompetences(getUserId(userIdHeader));
    }

    @GetMapping("/{id}")
    public CompetenceResponse getParId(@PathVariable("id") Long id) {
        return competenceService.getParId(id);
    }

    // ------------------------------------------------------------ création

    /** Création avec ou sans PDF (multipart). */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CompetenceResponse creer(
            @Valid @RequestPart("competence") CompetenceRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        return competenceService.creer(request, file, getUserId(userIdHeader));
    }

    /** Création sans PDF (JSON simple). */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CompetenceResponse creerSansFichier(
            @Valid @RequestBody CompetenceRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        return competenceService.creer(request, (MultipartFile) null, getUserId(userIdHeader));
    }

    // ------------------------------------------------------------ modification

    /** Modification avec ou sans PDF (multipart). */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CompetenceResponse modifier(
            @PathVariable("id") Long id,
            @Valid @RequestPart("competence") CompetenceRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        return competenceService.modifier(id, request, file, getUserId(userIdHeader));
    }

    /** Modification sans PDF (JSON simple). */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public CompetenceResponse modifierSansFichier(
            @PathVariable("id") Long id,
            @Valid @RequestBody CompetenceRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        return competenceService.modifier(id, request, (MultipartFile) null, getUserId(userIdHeader));
    }

    // ------------------------------------------------------------ suppression

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(
            @PathVariable("id") Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        competenceService.supprimer(id, getUserId(userIdHeader));
    }

    // ------------------------------------------------------------ documents PDF

    /** Ouvre un PDF enregistré (affiché dans le navigateur). */
    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<Resource> ouvrirFichier(@PathVariable("filename") String filename) {
        try {
            Path filePath = fileStorageLocation.resolve(filename).normalize();

            // Sécurité : on ne sort jamais du dossier de stockage (ex. filename = "../../secret")
            if (!filePath.startsWith(fileStorageLocation) || !Files.isRegularFile(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}