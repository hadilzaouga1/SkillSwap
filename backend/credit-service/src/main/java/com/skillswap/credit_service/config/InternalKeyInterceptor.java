package com.skillswap.credit_service.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Protège les routes /internal/** : seuls les autres microservices, qui connaissent la clé
 * partagée (en-tête X-Internal-Key), peuvent les appeler. La Gateway ne les expose pas.
 */
public class InternalKeyInterceptor implements HandlerInterceptor {

    public static final String ENTETE = "X-Internal-Key";

    private final byte[] cleAttendue;

    public InternalKeyInterceptor(String cle) {
        this.cleAttendue = cle.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        String recue = request.getHeader(ENTETE);
        // Comparaison à temps constant (pas de fuite d'information par le temps de réponse)
        if (recue != null && MessageDigest.isEqual(cleAttendue, recue.getBytes(StandardCharsets.UTF_8))) {
            return true;
        }
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"status\":403,\"message\":\"Accès réservé aux services internes\"}");
        return false;
    }
}
