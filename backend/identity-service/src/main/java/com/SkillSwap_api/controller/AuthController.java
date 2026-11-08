package com.SkillSwap_api.controller;

import com.SkillSwap_api.dto.AuthResponse;
import com.SkillSwap_api.dto.ConnexionRequest;
import com.SkillSwap_api.dto.InscriptionRequest;
import com.SkillSwap_api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** Routes PUBLIQUES d'authentification : /api/auth/** (laissées libres par la Gateway). */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentification")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "S'inscrire (US1)")
    @PostMapping("/inscription")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse inscription(@Valid @RequestBody InscriptionRequest request) {
        return authService.inscrire(request);
    }

    @Operation(summary = "Se connecter (US2)")
    @PostMapping("/connexion")
    public AuthResponse connexion(@Valid @RequestBody ConnexionRequest request) {
        return authService.connecter(request);
    }
}
