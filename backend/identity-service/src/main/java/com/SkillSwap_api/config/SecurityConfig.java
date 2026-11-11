package com.SkillSwap_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Active la config CORS définie dans CorsConfig
            .cors(cors -> {})
            
            // Désactive la protection CSRF (inutile pour les API REST sans session)
            .csrf(csrf -> csrf.disable())
            
            // Désactive la création de session (empêche le jsessionid dans l'URL)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Désactive la page de login par défaut et l'authentification Basic
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            
            // Autorise l'accès public à l'authentification et à Swagger
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/api/utilisateurs/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .anyRequest().authenticated()
            );

        return http.build();
    }
}