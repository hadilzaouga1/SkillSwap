package com.skillswap.credit_service.exception;

import java.util.Map;

/** Format unique des erreurs : { "status": 409, "message": "...", "erreurs": {...} } */
public record ApiError(int status, String message, Map<String, String> erreurs) {
}
