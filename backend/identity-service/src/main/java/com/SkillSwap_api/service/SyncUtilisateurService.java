package com.SkillSwap_api.service;

import com.SkillSwap_api.dto.SyncRequest;
import com.SkillSwap_api.dto.SyncResponse;
import com.SkillSwap_api.entity.Role;
import com.SkillSwap_api.entity.StatutCompte;
import com.SkillSwap_api.entity.Utilisateur;
import com.SkillSwap_api.exception.ApiException;
import com.SkillSwap_api.repository.UtilisateurRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SyncUtilisateurService {
    private final UtilisateurRepository repository;

    public SyncUtilisateurService(UtilisateurRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public SyncResponse synchroniser(SyncRequest req) {
        String email = req.email().trim().toLowerCase();

        Utilisateur u = repository.findByKeycloakId(req.keycloakId()).orElse(null);

        if (u == null) {
            u = repository.findByEmailIgnoreCase(email).orElse(null);
            if (u != null) {
                // An existing row (seed data): link it, but only for a verified email.
                if (!req.emailVerified()) {
                    throw new ApiException(HttpStatus.CONFLICT,
                            "Email non vérifié : impossible de rattacher ce compte");
                }
                if (u.getKeycloakId() != null) {
                    throw new ApiException(HttpStatus.CONFLICT,
                            "Cet email est déjà rattaché à un autre compte");
                }
                u.setKeycloakId(req.keycloakId());
            } else {
                u = new Utilisateur();
                u.setKeycloakId(req.keycloakId());
                u.setEmail(email);
                u.setNom(req.nom() == null || req.nom().isBlank()
                        ? email.substring(0, email.indexOf('@') > 0 ? email.indexOf('@') : email.length())
                        : req.nom().trim());
                u.setRole(Role.ETUDIANT);
                u.setStatut(StatutCompte.ACTIF);
                u.setNoteMoyenne(0.0);
            }
        }

        if (u.getStatut() == StatutCompte.SUSPENDU) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Ce compte est suspendu");
        }
        return new SyncResponse(repository.save(u).getId());
    }
}
