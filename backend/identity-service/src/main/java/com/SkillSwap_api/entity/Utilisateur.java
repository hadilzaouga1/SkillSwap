package com.SkillSwap_api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    /**
     * Mot de passe CHIFFRÉ avec BCrypt (jamais en clair, ENF1).
     * Exclu du toString() pour ne jamais apparaître dans les logs.
     * Aucun DTO ne le renvoie au frontend.
     */
    @ToString.Exclude
    @Column(name = "mot_de_passe", length = 255)
    private String motDePasse;

    /** Rôle : ETUDIANT par défaut. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private Role role = Role.ETUDIANT;

    /** Statut du compte : ACTIF par défaut. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private StatutCompte statut = StatutCompte.ACTIF;
}
