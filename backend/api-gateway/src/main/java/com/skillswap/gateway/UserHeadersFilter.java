package com.skillswap.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class UserHeadersFilter implements GlobalFilter, Ordered {

    private static final String HEADER_ID = "X-User-Id";
    private static final String HEADER_ROLE = "X-User-Role";

    private final IdentityClient identityClient;

    public UserHeadersFilter(IdentityClient identityClient) {
        this.identityClient = identityClient;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(ctx -> Mono.justOrEmpty(ctx.getAuthentication()))
                .filter(JwtAuthenticationToken.class::isInstance)
                .cast(JwtAuthenticationToken.class)
                .flatMap(auth -> identityClient.resolveId(auth.getToken())
                        .map(id -> avecEntetes(exchange, String.valueOf(id), roleLePlusEleve(auth))))
                .switchIfEmpty(Mono.fromSupplier(() -> avecEntetes(exchange, null, null)))
                .flatMap(chain::filter);
    }

    private String roleLePlusEleve(JwtAuthenticationToken auth) {
        var roles = auth.getAuthorities().stream().map(a -> a.getAuthority()).toList();
        if (roles.contains("ROLE_ADMIN")) return "ADMIN";
        if (roles.contains("ROLE_MODERATEUR")) return "MODERATEUR";
        return "ETUDIANT";
    }

    private ServerWebExchange avecEntetes(ServerWebExchange exchange, String id, String role) {
        ServerHttpRequest request = exchange.getRequest().mutate().headers(h -> {
            h.remove(HEADER_ID);
            h.remove(HEADER_ROLE);
            if (id != null) {
                h.set(HEADER_ID, id);
                h.set(HEADER_ROLE, role);
            }
        }).build();
        return exchange.mutate().request(request).build();
    }

    @Override
    public int getOrder() {
        return -100;
    }
}