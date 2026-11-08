package com.SkillSwap_api.service;

import com.SkillSwap_api.entity.StatutCompte;
import com.SkillSwap_api.entity.Utilisateur;
import com.SkillSwap_api.exception.ApiException;
import com.SkillSwap_api.repository.UtilisateurRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class UtilisateurCourantService {

    public static final String ENTETE = "X-User-Id";

    private final HttpServletRequest request;
    private final UtilisateurRepository utilisateurRepository;

    public UtilisateurCourantService(HttpServletRequest request, UtilisateurRepository utilisateurRepository) {
        this.request = request;
        this.utilisateurRepository = utilisateurRepository;
    }

    /** Renvoie l'utilisateur qui fait la requête, ou lance une erreur 401 / 403. */
    public Utilisateur getUtilisateurCourant() {
        String valeur = request.getHeader(ENTETE);
        if (valeur == null || valeur.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED,
                    "Utilisateur non identifié : en-tête " + ENTETE + " manquant");
        }

        long id;
        try {
            id = Long.parseLong(valeur.trim());
        } catch (NumberFormatException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "En-tête " + ENTETE + " invalide");
        }

        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Utilisateur inconnu"));

        if (utilisateur.getStatut() == StatutCompte.SUSPENDU) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Ce compte est suspendu");
        }
        return utilisateur;
    }
}
