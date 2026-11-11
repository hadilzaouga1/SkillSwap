package com.SkillSwap_api.repository;

import com.SkillSwap_api.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    /** Recherche par email sans tenir compte des majuscules (connexion). */
    Optional<Utilisateur> findByEmailIgnoreCase(String email);

    /** Vérifie l'unicité de l'email à l'inscription. */
    boolean existsByEmailIgnoreCase(String email);
}
