package com.redgit.ideas.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

@Service
public class TokenService {
    @Value("${security.jwt.secret-key}")
    private String secret;

    @PostConstruct
    private void validateSecret() {
        if (secret == null || secret.isBlank() || secret.length() < 32)
            throw new IllegalStateException("JWT_SECRET não configurado ou inválido. Defina a variável de ambiente JWT_SECRET com no mínimo 32 caracteres.");
    }

    public String validateToken(String token){
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("login-auth-api")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            return null;
        }
    }

    // Gera um token de curta duracao para chamadas service-to-service (ex.: notificar
    // eventos de reputacao em nome do autor de uma ideia). O claim "userId" e o
    // subject usados aqui sao o identificador do usuario destino do evento.
    public String generateServiceToken(String userId) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("login-auth-api")
                    .withSubject(userId)
                    .withClaim("userId", userId)
                    .withExpiresAt(Instant.now().plus(5, ChronoUnit.MINUTES))
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new IllegalStateException("Erro ao gerar token de servico", exception);
        }
    }

    private Instant generateExpirationDate(){
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }
}
