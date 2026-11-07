package com.skillswap.gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final String ENTETE_ID = "X-User-Id";
    private static final String ENTETE_ROLE = "X-User-Role";

    private final SecretKey cle;

    public JwtAuthenticationFilter(@Value("${skillswap.jwt.secret}") String secret) {
        // On affiche la clé au démarrage (les 5 premiers caractères) pour vérifier qu'elle est bien chargée
        System.out.println("🔑 Gateway initialisée avec le secret commençant par : " + secret.substring(0, Math.min(5, secret.length())));
        this.cle = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest requete = exchange.getRequest();

        if (HttpMethod.OPTIONS.equals(requete.getMethod())) {
            return chain.filter(exchange);
        }

        String chemin = requete.getPath().value();
        String authorization = requete.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        String userId = null;
        String role = null;

        if (authorization != null && authorization.startsWith("Bearer ")) {
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(cle)
                        .build()
                        .parseSignedClaims(authorization.substring(7))
                        .getPayload();
                userId = claims.getSubject();
                role = claims.get("role", String.class);
                
                System.out.println("✅ Token valide pour l'utilisateur : " + userId + " (Rôle : " + role + ")");
                
            } catch (JwtException | IllegalArgumentException e) {
                // C'EST ICI QU'ON AJOUTE LE LOG POUR VOIR L'ERREUR
                System.err.println("❌ ERREUR DE VALIDATION DU TOKEN sur " + chemin + " : " + e.getMessage());
                
                return erreur(exchange, HttpStatus.UNAUTHORIZED,
                        "Session expirée ou invalide, veuillez vous reconnecter");
            }
        }

        if (userId == null) {
            if (!estPublique(requete.getMethod(), chemin)) {
                System.err.println("⛔ Accès refusé (pas de token) sur : " + chemin);
                return erreur(exchange, HttpStatus.UNAUTHORIZED, "Authentification requise");
            }
        } else if (!roleAutorise(chemin, role)) {
            return erreur(exchange, HttpStatus.FORBIDDEN, "Accès refusé pour votre rôle");
        }

        final String idFinal = userId;
        final String roleFinal = (role != null) ? role : "ETUDIANT";

        ServerHttpRequest requeteModifiee = requete.mutate().headers(h -> {
            h.remove(ENTETE_ID);
            h.remove(ENTETE_ROLE);
            if (idFinal != null) {
                h.set(ENTETE_ID, idFinal);
                h.set(ENTETE_ROLE, roleFinal);
            }
        }).build();

        return chain.filter(exchange.mutate().request(requeteModifiee).build());
    }

    private boolean estPublique(HttpMethod methode, String chemin) {
        if (chemin.startsWith("/api/auth/")) {
            return true;
        }
        if (HttpMethod.GET.equals(methode)) {
            return chemin.equals("/api/competences")
                    || chemin.matches("/api/competences/\\d+")
                    || chemin.equals("/api/categories")
                    || chemin.matches("/api/utilisateurs/\\d+");
        }
        return false;
    }

    private boolean roleAutorise(String chemin, String role) {
        if (chemin.startsWith("/api/admin")) {
            return "ADMIN".equals(role);
        }
        if (chemin.startsWith("/api/moderation")) {
            return "MODERATEUR".equals(role) || "ADMIN".equals(role);
        }
        return true;
    }

    private Mono<Void> erreur(ServerWebExchange exchange, HttpStatus statut, String message) {
        ServerHttpResponse reponse = exchange.getResponse();
        reponse.setStatusCode(statut);
        reponse.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String json = "{\"status\":" + statut.value() + ",\"message\":\"" + message + "\"}";
        DataBuffer buffer = reponse.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        return reponse.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}