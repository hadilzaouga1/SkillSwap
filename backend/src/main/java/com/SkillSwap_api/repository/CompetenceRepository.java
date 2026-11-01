package com.SkillSwap_api.repository;

import com.SkillSwap_api.entity.Competence;
import com.SkillSwap_api.entity.Niveau;
import com.SkillSwap_api.entity.StatutValidation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompetenceRepository extends JpaRepository<Competence, Long> {

    /**
     * motCle : texte à chercher dans le titre OU la description ("" = pas de filtre).
     */
    @Query("""
            SELECT c FROM Competence c
            WHERE c.statutValidation = :statut
              AND (:categorieId IS NULL OR c.categorie.id = :categorieId)
              AND (:niveau IS NULL OR c.niveau = :niveau)
              AND (:etudiantId IS NULL OR c.etudiant.id = :etudiantId)
              AND (LOWER(c.titre) LIKE LOWER(CONCAT('%', :motCle, '%'))
                   OR LOWER(c.description) LIKE LOWER(CONCAT('%', :motCle, '%')))
            ORDER BY c.id DESC
            """)
    List<Competence> rechercher(@Param("statut") StatutValidation statut,
                                @Param("categorieId") Long categorieId,
                                @Param("niveau") Niveau niveau,
                                @Param("etudiantId") Long etudiantId,
                                @Param("motCle") String motCle);

    /** Toutes les compétences d'un étudiant, quel que soit leur statut (page "Mes compétences"). */
    List<Competence> findByEtudiantIdOrderByIdDesc(Long etudiantId);
}
