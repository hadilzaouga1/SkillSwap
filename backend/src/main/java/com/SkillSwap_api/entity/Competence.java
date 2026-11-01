package com.SkillSwap_api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Compétence (service) proposée par un étudiant. Table : competence.
 */
@Entity
@Table(name = "competence")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Competence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** DEBUTANT, INTERMEDIAIRE ou AVANCE (stocké en texte dans la base). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Niveau niveau;

    /** Validation par le modérateur . */
    @Enumerated(EnumType.STRING)
    @Column(name = "statut_validation", nullable = false, length = 20)
    private StatutValidation statutValidation = StatutValidation.EN_ATTENTE;

    /** Texte libre, ex. : "Lundi et mercredi soir". */
    @Column(length = 255)
    private String disponibilites;

    // EAGER : la catégorie est chargée en même temps que la compétence
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categorie_id", nullable = false)
    private Categorie categorie;

    // L'étudiant qui propose la compétence
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Utilisateur etudiant;
}
