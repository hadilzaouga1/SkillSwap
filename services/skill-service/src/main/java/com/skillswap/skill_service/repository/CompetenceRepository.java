package com.skillswap.skill_service.repository;

import com.skillswap.skill_service.entity.Competence;
import com.skillswap.skill_service.entity.Niveau;
import com.skillswap.skill_service.entity.StatutValidation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompetenceRepository extends JpaRepository<Competence, Long> {

    @Query("""
            SELECT c FROM Competence c
            WHERE c.statutValidation = :statut
              AND (:categorieId IS NULL OR c.categorie.id = :categorieId)
              AND (:niveau IS NULL OR c.niveau = :niveau)
              AND (:etudiantId IS NULL OR c.etudiantId = :etudiantId)
              AND (LOWER(c.titre) LIKE LOWER(CONCAT('%', :motCle, '%'))
                   OR LOWER(c.description) LIKE LOWER(CONCAT('%', :motCle, '%')))
            ORDER BY c.id DESC
            """)
    List<Competence> rechercher(@Param("statut") StatutValidation statut,
                                @Param("categorieId") Long categorieId,
                                @Param("niveau") Niveau niveau,
                                @Param("etudiantId") Long etudiantId,
                                @Param("motCle") String motCle);

    List<Competence> findByEtudiantIdOrderByIdDesc(Long etudiantId);
}
