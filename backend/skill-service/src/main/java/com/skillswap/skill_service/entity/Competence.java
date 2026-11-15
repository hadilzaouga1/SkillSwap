package com.skillswap.skill_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private Niveau niveau;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "statut_validation", nullable = false, length = 20)
    private StatutValidation statutValidation = StatutValidation.EN_ATTENTE;

    @Column(length = 255)
    private String disponibilites;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categorie_id", nullable = false)
    private Categorie categorie;

    @Column(name = "etudiant_id", nullable = false)
    private Long etudiantId;

    // Nouveaux champs pour le support PDF attaché à la compétence
    @Column(name = "document_url", length = 500)
    private String documentUrl;

    @Column(name = "document_name", length = 255)
    private String documentName;

    @Column(name = "stored_name", length = 255)
    private String storedName;

    @Column(name = "document_size")
    private Long documentSize;
}