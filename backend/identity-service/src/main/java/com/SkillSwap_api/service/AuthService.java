package com.SkillSwap_api.service;

import com.SkillSwap_api.dto.AuthResponse;
import com.SkillSwap_api.dto.ConnexionRequest;
import com.SkillSwap_api.dto.InscriptionRequest;
import com.SkillSwap_api.entity.Role;
import com.SkillSwap_api.entity.StatutCompte;
import com.SkillSwap_api.entity.Utilisateur;
import com.SkillSwap_api.exception.ApiException;
import com.SkillSwap_api.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/** Logique d'authentification : inscription (US1) et connexion (US2). */
@Service
public class AuthService {

    // Message volontairement identique pour "email inconnu" et "mauvais mot de passe"
    // (ne pas révéler quels emails existent).
    private static final String MESSAGE_IDENTIFIANTS = "Email ou mot de passe incorrect";

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final List<String> suffixesEmail;

    public AuthService(UtilisateurRepository utilisateurRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       @Value("${skillswap.auth.suffixes-email:.tn}") String suffixes) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        // ex. ".tn,.edu" -> [".tn", ".edu"] ; vide = aucune restriction
        this.suffixesEmail = Arrays.stream(suffixes.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    /** US1 : crée un compte ÉTUDIANT et connecte directement la personne. */
    @Transactional
    public AuthResponse inscrire(InscriptionRequest request) {
        String email = request.email().trim().toLowerCase();
        verifierEmailUniversitaire(email);

        if (utilisateurRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Un compte existe déjà avec cet email");
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(request.nom().trim());
        utilisateur.setEmail(email);
        utilisateur.setUniversite(request.universite());
        utilisateur.setFiliere(request.filiere());
        utilisateur.setMotDePasse(passwordEncoder.encode(request.motDePasse()));
        utilisateur.setRole(Role.ETUDIANT); // un visiteur ne peut JAMAIS choisir son rôle
        utilisateur.setStatut(StatutCompte.ACTIF);
        utilisateur.setNoteMoyenne(0.0);

        Utilisateur enregistre = utilisateurRepository.save(utilisateur);
        // Les crédits de bienvenue seront créés par le credit-service au premier accès au solde.
        return AuthResponse.de(jwtService.generer(enregistre), enregistre);
    }

    /** US2 : vérifie les identifiants et renvoie un token. */
    @Transactional(readOnly = true)
    public AuthResponse connecter(ConnexionRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, MESSAGE_IDENTIFIANTS));

        boolean motDePasseValide = utilisateur.getMotDePasse() != null
                && passwordEncoder.matches(request.motDePasse(), utilisateur.getMotDePasse());
        if (!motDePasseValide) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, MESSAGE_IDENTIFIANTS);
        }

        if (utilisateur.getStatut() == StatutCompte.SUSPENDU) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Ce compte est suspendu");
        }

        return AuthResponse.de(jwtService.generer(utilisateur), utilisateur);
    }

    /** Email universitaire : doit se terminer par un des suffixes configurés (ex. ".tn"). */
    private void verifierEmailUniversitaire(String email) {
        if (suffixesEmail.isEmpty()) {
            return;
        }
        boolean valide = suffixesEmail.stream().anyMatch(email::endsWith);
        if (!valide) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Un email universitaire est requis (se terminant par : " + String.join(", ", suffixesEmail) + ")");
        }
    }
}
