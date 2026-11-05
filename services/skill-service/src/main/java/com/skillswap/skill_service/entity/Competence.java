package com.skillswap.skill_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Niveau niveau;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_validation", nullable = false, length = 20)
    private StatutValidation statutValidation = StatutValidation.EN_ATTENTE;

    @Column(length = 255)
    private String disponibilites;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categorie_id", nullable = false)
    private Categorie categorie;

    // On remplace Utilisateur par un simple ID (le User Service gérera les détails)
    @Column(name = "etudiant_id", nullable = false)
    private Long etudiantId;
}