package com.skillswap.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IdentityClient {

    public record SyncResponse(Long id) {}

    private record Cached(Long id, long expiresAt) {}

    private static final long TTL_MS = 60_000;

    private final WebClient webClient;
    private final String internalKey;
    private final ConcurrentHashMap<String, Cached> cache = new ConcurrentHashMap<>();

    public IdentityClient(WebClient.Builder builder,
                          @Value("${skillswap.identity.url}") String url,
                          @Value("${skillswap.identity.internal-key}") String internalKey) {
        this.webClient = builder.baseUrl(url).build();
        this.internalKey = internalKey;
    }

    public Mono<Long> resolveId(Jwt jwt) {
        String sub = jwt.getSubject();
        Cached c = cache.get(sub);
        if (c != null && c.expiresAt() > System.currentTimeMillis()) {
            return Mono.just(c.id());
        }

        String nom = jwt.getClaimAsString("name");
        if (nom == null || nom.isBlank()) {
            nom = jwt.getClaimAsString("preferred_username");
        }
        String email = jwt.getClaimAsString("email");
        Map<String, Object> body = Map.of(
                "keycloakId", sub,
                "email", email == null ? "" : email,
                "nom", nom == null ? "" : nom,
                "emailVerified", Boolean.TRUE.equals(jwt.getClaimAsBoolean("email_verified")));

        return webClient.post()
                .uri("/internal/users/sync")
                .header("X-Internal-Key", internalKey)
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, r -> Mono.error(
                        new ResponseStatusException(r.statusCode(), "Compte non autorisé")))
                .bodyToMono(SyncResponse.class)
                .map(SyncResponse::id)
                .doOnNext(id -> cache.put(sub, new Cached(id, System.currentTimeMillis() + TTL_MS)))
                .onErrorMap(e -> !(e instanceof ResponseStatusException),
                        e -> new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                                "identity-service indisponible"));
    }
}