package com.SkillSwap_api.service;

import com.SkillSwap_api.entity.Utilisateur;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Fabrique les tokens JWT (signés HS256).
 * Le token contient : l'id (subject), le rôle et le nom.
 * C'est l'API Gateway qui le VÉRIFIE ensuite, avec le MÊME secret
 * (propriété skillswap.jwt.secret, identique dans les deux services).
 */
@Service
public class JwtService {

    private final SecretKey cle;
    private final long dureeMillisecondes;

    public JwtService(@Value("${skillswap.jwt.secret}") String secret,
                      @Value("${skillswap.jwt.duree-minutes:120}") long dureeMinutes) {
        // HS256 exige une clé d'au moins 32 octets
        this.cle = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.dureeMillisecondes = dureeMinutes * 60_000L;
    }

    public String generer(Utilisateur utilisateur) {
        Date maintenant = new Date();
        return Jwts.builder()
                .subject(String.valueOf(utilisateur.getId()))
                .claim("role", utilisateur.getRole().name())
                .claim("nom", utilisateur.getNom())
                .issuedAt(maintenant)
                .expiration(new Date(maintenant.getTime() + dureeMillisecondes))
                .signWith(cle, Jwts.SIG.HS256)
                .compact();
    }
}
