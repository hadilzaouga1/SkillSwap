package com.SkillSwap_api.exception;

import java.util.Map;

/**
 * Format unique des erreurs renvoyées par l'API :
 * { "status": 400, "message": "...", "erreurs": { "titre": "Le titre est obligatoire" } }
 * "erreurs" n'est rempli que pour les erreurs de validation de formulaire.
 */
public record ApiError(int status, String message, Map<String, String> erreurs) {
}
