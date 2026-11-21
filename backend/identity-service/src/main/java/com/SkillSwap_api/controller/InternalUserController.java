package com.SkillSwap_api.controller;

import com.SkillSwap_api.dto.SyncRequest;
import com.SkillSwap_api.dto.SyncResponse;
import com.SkillSwap_api.exception.ApiException;
import com.SkillSwap_api.service.SyncUtilisateurService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/internal/users")
public class InternalUserController {
    private final SyncUtilisateurService service;
    private final byte[] internalKey;

    public InternalUserController(SyncUtilisateurService service,
                                  @Value("${skillswap.internal-key}") String internalKey) {
        this.service = service;
        this.internalKey = internalKey.getBytes(StandardCharsets.UTF_8);
    }

    @PostMapping("/sync")
    public SyncResponse sync(@RequestHeader(value = "X-Internal-Key", required = false) String key,
                             @Valid @RequestBody SyncRequest request) {
        if (key == null || !MessageDigest.isEqual(key.getBytes(StandardCharsets.UTF_8), internalKey)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Clé interne invalide");
        }
        try {
            return service.synchroniser(request);
        } catch (DataIntegrityViolationException e) {
            // Two parallel first requests: the second one now finds the row created by the first.
            return service.synchroniser(request);
        }
    }
}
