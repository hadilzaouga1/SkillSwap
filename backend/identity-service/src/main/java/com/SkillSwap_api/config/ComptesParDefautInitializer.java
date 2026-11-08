package com.SkillSwap_api.config;

import com.SkillSwap_api.entity.Role;
import com.SkillSwap_api.entity.StatutCompte;
import com.SkillSwap_api.entity.Utilisateur;
import com.SkillSwap_api.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MODE DÉVELOPPEMENT : crée (ou complète) des comptes de test au démarrage.
 *  - admin@skillswap.tn       (ADMIN)
 *  - moderateur@skillswap.tn  (MODERATEUR)
 *  - hadil@univ.tn, roua@univ.tn (ETUDIANT)
 * Le mot de passe de test vient de la propriété skillswap.dev.mot-de-passe.
 *
 * Un utilisateur déjà présent en base mais sans mot de passe (ex. données de départ)
 * reçoit simplement un mot de passe : ses compétences restent liées à lui.
 *
 * EN PRODUCTION : mettre skillswap.dev.creer-comptes-par-defaut=false
 * (les modérateurs seront créés par l'administrateur, US23).
 */
@Component
public class ComptesParDefautInitializer implements ApplicationRunner {

    private record CompteDefaut(String nom, String email, Role role, String universite) {
    }

    private static final List<CompteDefaut> COMPTES = List.of(
            new CompteDefaut("Administrateur", "admin@skillswap.tn", Role.ADMIN, null),
            new CompteDefaut("Modérateur", "moderateur@skillswap.tn", Role.MODERATEUR, null),
            new CompteDefaut("Hadil", "hadil@univ.tn", Role.ETUDIANT, "Tunis"),
            new CompteDefaut("Roua", "roua@univ.tn", Role.ETUDIANT, "Tunis")
    );

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean actif;
    private final String motDePasseTest;

    public ComptesParDefautInitializer(
            UtilisateurRepository utilisateurRepository,
            PasswordEncoder passwordEncoder,
            @Value("${skillswap.dev.creer-comptes-par-defaut:false}") boolean actif,
            @Value("${skillswap.dev.mot-de-passe:Password123!}") String motDePasseTest) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.actif = actif;
        this.motDePasseTest = motDePasseTest;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!actif) {
            return;
        }
        for (CompteDefaut compte : COMPTES) {
            Utilisateur utilisateur = utilisateurRepository.findByEmailIgnoreCase(compte.email())
                    .orElseGet(() -> {
                        Utilisateur nouveau = new Utilisateur();
                        nouveau.setNom(compte.nom());
                        nouveau.setEmail(compte.email());
                        nouveau.setUniversite(compte.universite());
                        nouveau.setRole(compte.role());
                        nouveau.setStatut(StatutCompte.ACTIF);
                        nouveau.setNoteMoyenne(0.0);
                        return nouveau;
                    });

            boolean aCompleter = utilisateur.getId() == null || utilisateur.getMotDePasse() == null;
            if (aCompleter) {
                utilisateur.setMotDePasse(passwordEncoder.encode(motDePasseTest));
                utilisateurRepository.save(utilisateur);
            }
        }
    }
}
