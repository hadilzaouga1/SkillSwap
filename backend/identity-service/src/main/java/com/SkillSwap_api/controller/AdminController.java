package com.SkillSwap_api.controller;

import com.SkillSwap_api.dto.AdminStatsResponse;
import com.SkillSwap_api.repository.UtilisateurRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UtilisateurRepository utilisateurRepository;

    public AdminController(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @GetMapping("/stats")
    public AdminStatsResponse getStats() {
        // Pour l'instant, on compte juste les utilisateurs de cette base.
        // Les autres chiffres (compétences, échanges) viendront plus tard via Feign.
        long totalUtilisateurs = utilisateurRepository.count();

        return new AdminStatsResponse(
                totalUtilisateurs,
                0, // À remplacer par un appel à skill-service
                0, // À remplacer par un appel à skill-service
                0  // À remplacer par un appel à exchange-service
        );
    }
}
