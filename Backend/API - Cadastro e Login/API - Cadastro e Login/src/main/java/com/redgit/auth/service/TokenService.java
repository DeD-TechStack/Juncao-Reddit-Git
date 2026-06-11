package com.redgit.auth.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.redgit.auth.infrastructure.entity.User;
import com.redgit.auth.infrastructure.redis.RedisService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    @Value("${security.jwt.secret-key}")
    private String secret;

    @Value("${cache.token.ttl:900}")
    private long tokenCacheTTL;

    private final RedisService redisService;

    @PostConstruct
    private void validateSecret() {
        if (secret == null || secret.isBlank() || secret.length() < 32)
            throw new IllegalStateException("JWT_SECRET não configurado ou inválido. Defina a variável de ambiente JWT_SECRET com no mínimo 32 caracteres.");
    }

    public String generateToken(User user){
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            String token = JWT.create()
                    .withIssuer("login-auth-api")
                    .withSubject(user.getEmail())
                    .withClaim("userId", user.getId().toString())
                    .withExpiresAt(this.generateExpirationDate())
                    .sign(algorithm);

            log.debug("Token gerado para: {}", user.getEmail());
            return token;
        } catch (JWTCreationException exception){
            log.error("Erro ao gerar token: {}", exception.getMessage());
            throw new RuntimeException("Error while authenticating");
        }
    }

    public String validateToken(String token){
        if (token == null || token.isEmpty()) {
            return null;
        }

        try {
            // 1. Verifica se está na blacklist
            if (isTokenBlacklisted(token)) {
                log.warn("Token está na blacklist");
                return null;
            }

            // 2. Verifica se está no cache
            String cachedEmail = getCachedToken(token);
            if (cachedEmail != null) {
                log.debug("Token validado via cache: {}", cachedEmail);
                return cachedEmail;
            }

            // 3. Valida o token via JWT
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String email = JWT.require(algorithm)
                    .withIssuer("login-auth-api")
                    .build()
                    .verify(token)
                    .getSubject();

            // 4. Armazena no cache para próximas validações
            if (email != null) {
                cacheToken(token, email);
                log.debug("Token validado e cacheado: {}", email);
            }

            return email;

        } catch (JWTVerificationException exception) {
            log.debug("Token inválido: {}", exception.getMessage());
            return null;
        }
    }

    public void blacklistToken(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }

        String blacklistKey = "blacklist:" + token;

        // Calcula o tempo restante do token para definir o TTL da blacklist
        long ttl = getTokenRemainingTime(token);

        if (ttl > 0) {
            redisService.set(blacklistKey, true, ttl);

            // Remove do cache de validação
            String cacheKey = "token:" + token;
            redisService.delete(cacheKey);

            log.info("Token adicionado à blacklist (TTL: {}s)", ttl);
        }
    }

    public boolean isTokenBlacklisted(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        String blacklistKey = "blacklist:" + token;
        return redisService.exists(blacklistKey);
    }

    private void cacheToken(String token, String email) {
        String cacheKey = "token:" + token;
        redisService.set(cacheKey, email, tokenCacheTTL);
    }

    private String getCachedToken(String token) {
        String cacheKey = "token:" + token;
        return redisService.get(cacheKey, String.class);
    }

    private long getTokenRemainingTime(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            Instant expiresAt = JWT.require(algorithm)
                    .withIssuer("login-auth-api")
                    .build()
                    .verify(token)
                    .getExpiresAtAsInstant();

            if (expiresAt != null) {
                long remaining = expiresAt.getEpochSecond() - Instant.now().getEpochSecond();
                return Math.max(0, remaining);
            }
        } catch (JWTVerificationException e) {
            log.debug("Erro ao calcular tempo restante do token: {}", e.getMessage());
        }

        return 0;
    }

    private Instant generateExpirationDate(){
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }
}
