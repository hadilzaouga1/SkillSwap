package com.SkillSwap_api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "utilisateur")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false, unique = true)
    private String email;

    private String universite;
    private String filiere;

    @Column(name = "note_moyenne")
    private Double noteMoyenne;

    /** Rôle : ETUDIANT par défaut. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.ETUDIANT;

    /** Statut du compte : ACTIF par défaut. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutCompte statut = StatutCompte.ACTIF;
}
