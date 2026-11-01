package com.SkillSwap_api.config;

import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ajoute le champ "X-User-Id" à toutes les routes dans Swagger
 * (http://localhost:8081/swagger-ui.html) pour pouvoir tester
 * en se faisant passer pour un utilisateur, avant l'authentification.
 *
 * TODO dernier sprint : supprimer ce bean quand l'authentification JWT sera en place.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OperationCustomizer ajouterEnteteUtilisateur() {
        return (operation, handlerMethod) -> {
            operation.addParametersItem(new HeaderParameter()
                    .name("X-User-Id")
                    .description("Id de l'utilisateur simulé (ex. 1 = Hadil, 2 = Roua). Temporaire, avant l'authentification.")
                    .required(false)
                    .schema(new StringSchema()));
            return operation;
        };
    }
}
