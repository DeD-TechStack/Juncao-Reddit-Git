package com.daniel.registry.trending.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    @Value("${security.jwt.secret-key}")
    private String secret;

    @PostConstruct
    private void validateSecret() {
        if (secret == null || secret.isBlank() || secret.length() < 32)
            throw new IllegalStateException("SECURITY_JWT_SECRET_KEY não configurado ou inválido. Defina a variável com no mínimo 32 caracteres.");
    }

    public String validateToken(String token) {
        try {
            var decoded = JWT.require(Algorithm.HMAC256(secret))
                    .withIssuer("login-auth-api")
                    .build()
                    .verify(token);
            return decoded.getClaim("userId").asString();
        } catch (JWTVerificationException e) {
            return null;
        }
    }
}
