package com.SkillSwap_api.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Centralise la gestion des erreurs pour TOUS les contrôleurs.
 * Le frontend reçoit toujours le même format (voir ApiError).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Erreurs métier lancées volontairement (404, 403, 401...). */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApi(ApiException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(new ApiError(ex.getStatus().value(), ex.getMessage(), null));
    }

    /** Formulaire invalide (annotations @NotBlank, @Size... non respectées). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> erreurs = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> erreurs.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(new ApiError(400, "Données invalides", erreurs));
    }

    /** JSON mal formé ou valeur d'énumération inconnue (ex. niveau = "EXPERT"). */
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiError> handleMalformed(Exception ex) {
        return ResponseEntity.badRequest()
                .body(new ApiError(400, "Requête invalide : vérifiez le format des valeurs envoyées", null));
    }

    /** Contrainte de la base violée (ex. suppression d'une compétence utilisée par un échange). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleIntegrity(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError(409, "Opération impossible : cet élément est utilisé ailleurs ou existe déjà", null));
    }
}
