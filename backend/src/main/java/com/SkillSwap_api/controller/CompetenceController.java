// Déclaration du package auquel appartient ce contrôleur
package com.SkillSwap_api.controller;

// Importation des DTO utilisés pour recevoir et retourner les données
import com.SkillSwap_api.dto.CompetenceRequest;
import com.SkillSwap_api.dto.CompetenceResponse;

// Importation de l'énumération représentant le niveau de compétence
import com.SkillSwap_api.entity.Niveau;

// Importation du service qui contient la logique métier des compétences
import com.SkillSwap_api.service.CompetenceService;

// Importation des annotations Swagger pour documenter l'API
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

// Permet de vérifier les données reçues dans les requêtes
import jakarta.validation.Valid;

// Importation du code HTTP pour définir le statut des réponses
import org.springframework.http.HttpStatus;

// Importation des annotations Spring Web
import org.springframework.web.bind.annotation.*;

// Permet de retourner une liste de compétences
import java.util.List;

/**
 * Contrôleur REST responsable de la gestion des compétences.
 * Toutes les routes commencent par /api/competences.
 */
@RestController // Indique que cette classe est un contrôleur REST.
               // Les méthodes retournent directement des données (JSON).
@RequestMapping("/api/competences") // Définit le chemin commun de toutes les routes.
@Tag(name = "Compétences") // Regroupe les endpoints sous le nom "Compétences" dans Swagger.
public class CompetenceController {

    // Déclaration du service utilisé pour gérer les compétences.
    private final CompetenceService competenceService;

    // Constructeur : permet d'injecter automatiquement le service.
    // Spring fournit une instance de CompetenceService.
    public CompetenceController(CompetenceService competenceService) {
        this.competenceService = competenceService;
    }

    // Documentation de l'opération dans Swagger.
    @Operation(
        summary = "Catalogue / recherche (US7)",
        description = "Tous les paramètres sont optionnels. Sans paramètre : tout le catalogue validé."
    )

    // GET : permet de rechercher et consulter le catalogue des compétences.
    @GetMapping
    public List<CompetenceResponse> rechercher(

            // Filtrer les compétences selon l'identifiant de leur catégorie.
            // required = false : ce paramètre est facultatif.
            @RequestParam(name = "categorieId", required = false) Long categorieId,

            // Filtrer les compétences selon leur niveau.
            @RequestParam(name = "niveau", required = false) Niveau niveau,

            // Rechercher une compétence à partir d'un mot-clé.
            @RequestParam(name = "motCle", required = false) String motCle,

            // Filtrer les compétences selon l'identifiant de l'étudiant.
            @RequestParam(name = "etudiantId", required = false) Long etudiantId) {

        // Appeler le service pour effectuer la recherche
        // et retourner la liste des compétences correspondantes.
        return competenceService.rechercher(categorieId, niveau, motCle, etudiantId);
    }

    // Documentation de l'opération dans Swagger.
    @Operation(summary = "Mes compétences (tous statuts)")

    // GET : récupérer toutes les compétences de l'étudiant connecté,
    // quel que soit leur statut.
    @GetMapping("/mes-competences")
    public List<CompetenceResponse> mesCompetences() {

        // Appeler le service pour récupérer les compétences personnelles.
        return competenceService.getMesCompetences();
    }

    // Documentation de l'opération dans Swagger.
    @Operation(summary = "Détail d'une compétence")

    // GET : récupérer les détails d'une compétence à partir de son ID.
    @GetMapping("/{id}")
    public CompetenceResponse getParId(

            // @PathVariable récupère l'identifiant depuis l'URL.
            @PathVariable("id") Long id) {

        // Appeler le service pour rechercher la compétence correspondante.
        return competenceService.getParId(id);
    }

    // Documentation de l'opération dans Swagger.
    @Operation(summary = "Publier une compétence (US5)")

    // POST : créer et publier une nouvelle compétence.
    @PostMapping

    // Indiquer que la création a réussi avec le statut HTTP 201 (Created).
    @ResponseStatus(HttpStatus.CREATED)
    public CompetenceResponse creer(

            // @RequestBody récupère les données envoyées dans le corps JSON.
            // @Valid déclenche la validation des données selon les contraintes du DTO.
            @Valid @RequestBody CompetenceRequest request) {

        // Appeler le service pour créer la compétence
        // et retourner les informations de la compétence créée.
        return competenceService.creer(request);
    }

    // Documentation de l'opération dans Swagger.
    @Operation(summary = "Modifier ma compétence (US6)")

    // PUT : modifier une compétence existante à partir de son ID.
    @PutMapping("/{id}")
    public CompetenceResponse modifier(

            // Récupérer l'identifiant de la compétence depuis l'URL.
            @PathVariable("id") Long id,

            // Récupérer et valider les nouvelles données envoyées en JSON.
            @Valid @RequestBody CompetenceRequest request) {

        // Appeler le service pour effectuer la modification.
        return competenceService.modifier(id, request);
    }

    // Documentation de l'opération dans Swagger.
    @Operation(summary = "Supprimer ma compétence (US6)")

    // DELETE : supprimer une compétence à partir de son ID.
    @DeleteMapping("/{id}")

    // Indiquer que la suppression a réussi avec le statut HTTP 204 (No Content).
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(

            // Récupérer l'identifiant de la compétence à supprimer.
            @PathVariable("id") Long id) {

        // Appeler le service pour supprimer la compétence.
        // void signifie que cette méthode ne retourne aucune donnée.
        competenceService.supprimer(id);
    }
}