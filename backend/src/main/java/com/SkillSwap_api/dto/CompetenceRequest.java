package com.SkillSwap_api.dto;
import com.SkillSwap_api.entity.Niveau;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO (Data Transfer Object) :
 * Objet utilisé pour recevoir les données envoyées par le frontend
 * lors de la publication (POST) ou de la modification (PUT) d'une compétence.
 *
 * Les annotations de validation sont déclenchées grâce à @Valid
 * dans le contrôleur.
 *
 * Si les données ne respectent pas les contraintes,
 * l'API retourne une erreur HTTP 400 (Bad Request).
 */

// record : permet de créer une classe contenant principalement des données.
// Java génère automatiquement le constructeur, les accesseurs,
// ainsi que equals(), hashCode() et toString().
public record CompetenceRequest(

        // Titre de la compétence.
        // @NotBlank : le titre est obligatoire et ne doit pas être vide
        // ou contenir uniquement des espaces.
        @NotBlank(message = "Le titre est obligatoire")

        // @Size : limite la longueur du titre à 120 caractères maximum.
        @Size(max = 120, message = "Le titre ne doit pas dépasser 120 caractères")
        String titre,


        // Description de la compétence.
        // Ce champ est facultatif.
        // Sa longueur ne doit pas dépasser 2000 caractères.
        @Size(max = 2000, message = "La description ne doit pas dépasser 2000 caractères")
        String description,


        // Niveau de la compétence (par exemple : Débutant, Intermédiaire, Avancé).
        // @NotNull : oblige l'utilisateur à choisir un niveau.
        @NotNull(message = "Le niveau est obligatoire")
        Niveau niveau,
        @NotNull(message = "La catégorie est obligatoire")
        Long categorieId,
        @Size(max = 255, message = "Les disponibilités ne doivent pas dépasser 255 caractères")
        String disponibilites

) {
}