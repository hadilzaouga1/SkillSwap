package com.SkillSwap_api.dto;
import com.SkillSwap_api.entity.Competence;
import com.SkillSwap_api.entity.Niveau;
import com.SkillSwap_api.entity.StatutValidation;

/**
 * DTO (Data Transfer Object) :
 * Objet utilisé pour envoyer les informations d'une compétence au frontend.
 *
 * On n'expose pas directement les entités JPA.
 * On choisit ici les informations que l'API peut retourner.
 *
 * Par exemple : on retourne le nom de l'étudiant,
 * mais pas son adresse email.
 */

// record : permet de créer un objet contenant des données.
// Java génère automatiquement le constructeur, les accesseurs,
// ainsi que equals(), hashCode() et toString().
public record CompetenceResponse(

        // Identifiant de la compétence.
        Long id,

        // Titre de la compétence.
        String titre,

        // Description de la compétence.
        String description,

        // Niveau de la compétence (Débutant, Intermédiaire, Avancé...).
        Niveau niveau,

        // Disponibilités de l'étudiant pour partager cette compétence.
        String disponibilites,

        // Statut de validation de la compétence.
        // Exemple : EN_ATTENTE, VALIDEE ou REFUSEE.
        StatutValidation statutValidation,

        // Informations sur la catégorie de la compétence.
        CategorieInfo categorie,

        // Informations sur l'étudiant qui propose la compétence.
        EtudiantInfo etudiant

) {

    /**
     * Record interne représentant les informations d'une catégorie.
     * On retourne uniquement son identifiant et son nom.
     */
    public record CategorieInfo(

            // Identifiant de la catégorie.
            Long id,

            // Nom de la catégorie.
            String nom

    ) {
        // Fin du record CategorieInfo.
    }


    /**
     * Record interne représentant les informations d'un étudiant.
     * On retourne uniquement son identifiant et son nom.
     */
    public record EtudiantInfo(

            // Identifiant de l'étudiant.
            Long id,

            // Nom de l'étudiant.
            String nom

    ) {
        // Fin du record EtudiantInfo.
    }


    /**
     * Méthode statique permettant de convertir une entité Competence
     * en un objet CompetenceResponse.
     *
     * Elle récupère les informations depuis l'entité
     * et construit le DTO qui sera envoyé au frontend.
     */

    // static : cette méthode appartient à la classe.
    // Elle peut être appelée sans créer une instance de CompetenceResponse.
    public static CompetenceResponse depuis(Competence c) {

        // Création d'un nouvel objet CompetenceResponse
        // en récupérant les informations depuis l'entité Competence.
        return new CompetenceResponse(
                c.getId(),
                c.getTitre(),
                c.getDescription(),
                c.getNiveau(),
                c.getDisponibilites(),
                c.getStatutValidation(),
                new CategorieInfo(
                        c.getCategorie().getId(),
                        c.getCategorie().getNom()
                ),

                // Créer un objet EtudiantInfo avec l'ID et le nom de l'étudiant.
                new EtudiantInfo(
                        c.getEtudiant().getId(),
                        c.getEtudiant().getNom()
                )
        );
    }
}