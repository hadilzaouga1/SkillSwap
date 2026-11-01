package com.SkillSwap_api.service;

import com.SkillSwap_api.dto.ProfilPublicResponse;
import com.SkillSwap_api.dto.ProfilResponse;
import com.SkillSwap_api.dto.ProfilUpdateRequest;
import com.SkillSwap_api.entity.Utilisateur;
import com.SkillSwap_api.exception.ApiException;
import com.SkillSwap_api.repository.UtilisateurRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//Logique métier des profils 
@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurCourantService utilisateurCourantService;

    public UtilisateurService(UtilisateurRepository utilisateurRepository,
                              UtilisateurCourantService utilisateurCourantService) {
        this.utilisateurRepository = utilisateurRepository;
        this.utilisateurCourantService = utilisateurCourantService;
    }

    public ProfilResponse getMonProfil() {
        return ProfilResponse.depuis(utilisateurCourantService.getUtilisateurCourant());
    }

    @Transactional
    public ProfilResponse modifierMonProfil(ProfilUpdateRequest request) {
        Utilisateur moi = utilisateurCourantService.getUtilisateurCourant();
        moi.setNom(request.nom().trim());
        moi.setUniversite(request.universite());
        moi.setFiliere(request.filiere());
        return ProfilResponse.depuis(utilisateurRepository.save(moi));
    }

    //profil d'un autre étudiant 
    @Transactional(readOnly = true)
    public ProfilPublicResponse getProfilPublic(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        return ProfilPublicResponse.depuis(utilisateur);
    }
}
