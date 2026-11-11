package com.skillswap.credit_service.exception;

import org.springframework.http.HttpStatus;

/** Erreur métier : le GlobalExceptionHandler la transforme en réponse JSON avec le bon code HTTP. */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
