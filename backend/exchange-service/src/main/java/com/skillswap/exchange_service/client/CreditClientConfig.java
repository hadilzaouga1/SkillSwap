package com.skillswap.exchange_service.client;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

/**
 * Configuration propre au client credit-service : ajoute la clé interne partagée à chaque appel.
 * Volontairement SANS @Configuration : elle ne doit s'appliquer qu'à CreditClient
 * (la clé ne doit pas être envoyée à skill-service).
 */
public class CreditClientConfig {

    @Bean
    public RequestInterceptor internalKeyInterceptor(
            @Value("${skillswap.internal.key:dev-internal-key-change-me}") String internalKey) {
        return template -> template.header("X-Internal-Key", internalKey);
    }
}
