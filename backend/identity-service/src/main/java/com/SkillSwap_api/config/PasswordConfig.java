package com.SkillSwap_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Outil de chiffrement des mots de passe (BCrypt).
 * On utilise uniquement "spring-security-crypto" : pas besoin de tout Spring Security ici,
 * car les routes sont protégées par l'API Gateway.
 */
@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
