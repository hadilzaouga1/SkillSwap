package com.skillswap.gateway;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Réponses "plan B" du Circuit Breaker du gateway (équivalent de /defaultMuslim dans le TP).
 *
 * Règle : on renvoie des DONNÉES PAR DÉFAUT seulement pour les lectures sans danger
 * (catégories, liste des compétences, notifications). Pour tout le reste (crédits, échanges,
 * création/modification...) on renvoie une vraie erreur 503 : inventer une réponse serait faux.
 * Les réponses "par défaut" portent l'en-tête X-Fallback: true.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    // Mêmes catégories que la migration V2__seed_categories.sql de skill-service
    private static final List<Map<String, Object>> CATEGORIES_PAR_DEFAUT = List.of(
            categorie(3, "Design"),
            categorie(1, "Informatique"),
            categorie(2, "Langues"),
            categorie(4, "Mathématiques"),
            categorie(5, "Musique"));

    /** GET /api/categories : la liste fixe des catégories. */
    @RequestMapping("/categories")
    public Mono<ResponseEntity<Object>> categories(ServerHttpRequest request) {
        return lectureSeulement(request, "skill", CATEGORIES_PAR_DEFAUT);
    }

    /** GET /api/competences et /api/competences/mes-competences : liste vide (l'écran ne plante pas). */
    @RequestMapping("/competences")
    public Mono<ResponseEntity<Object>> competences(ServerHttpRequest request) {
        return lectureSeulement(request, "skill", List.of());
    }

    /** GET /api/notifications : aucune notification. */
    @RequestMapping("/notifications")
    public Mono<ResponseEntity<Object>> notifications(ServerHttpRequest request) {
        return lectureSeulement(request, "notification", List.of());
    }

    /** GET /api/notifications/unread-count : 0 non lue (même forme que la vraie réponse). */
    @RequestMapping("/notifications-unread")
    public Mono<ResponseEntity<Object>> notificationsNonLues(ServerHttpRequest request) {
        return lectureSeulement(request, "notification", Map.of("unreadCount", 0));
    }

    /** Tout le reste (identity, exchange, credit, session, trust...) : erreur 503 claire. */
    @RequestMapping("/{service}")
    public Mono<ResponseEntity<Object>> indisponible(@PathVariable String service) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("status", 503);
        corps.put("service", service + "-service");
        corps.put("message", "Le service " + service + " est momentanément indisponible, veuillez réessayer plus tard.");
        corps.put("timestamp", Instant.now().toString());
        ResponseEntity<Object> reponse = ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(corps);
        return Mono.just(reponse);
    }

    private Mono<ResponseEntity<Object>> lectureSeulement(ServerHttpRequest request, String service, Object parDefaut) {
        if (HttpMethod.GET.equals(request.getMethod())) {
            ResponseEntity<Object> reponse = ResponseEntity.ok().header("X-Fallback", "true").body(parDefaut);
            return Mono.just(reponse);
        }
        return indisponible(service);   // POST, PATCH, DELETE... : jamais de fausse réponse
    }

    private static Map<String, Object> categorie(int id, String nom) {
        Map<String, Object> c = new LinkedHashMap<>();
        c.put("id", id);
        c.put("nom", nom);
        return c;
    }
}
