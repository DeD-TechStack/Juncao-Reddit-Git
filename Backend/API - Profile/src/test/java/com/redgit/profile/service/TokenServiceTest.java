package com.redgit.profile.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenService Tests")
class TokenServiceTest {

    @InjectMocks
    private TokenService tokenService;

    private static final String TEST_SECRET = "3246918694727278232479912314703835454208642542872406260685881546";
    private static final String TEST_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tokenService, "secret", TEST_SECRET);
    }

    @Test
    @DisplayName("Deve validar um token JWT válido e retornar o email do usuário")
    void validateToken_withValidToken_shouldReturnEmail() {
        // Arrange - criar um token válido
        Algorithm algorithm = Algorithm.HMAC256(TEST_SECRET);
        String validToken = JWT.create()
                .withIssuer("login-auth-api")
                .withSubject(TEST_EMAIL)
                .withExpiresAt(Instant.now().plus(2, ChronoUnit.HOURS))
                .sign(algorithm);

        // Act
        String result = tokenService.validateToken(validToken);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_EMAIL, result);
    }

    @Test
    @DisplayName("Deve retornar null para um token inválido")
    void validateToken_withInvalidToken_shouldReturnNull() {
        // Arrange
        String invalidToken = "token.invalido.fake";

        // Act
        String result = tokenService.validateToken(invalidToken);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Deve retornar null para um token expirado")
    void validateToken_withExpiredToken_shouldReturnNull() {
        // Arrange - token expirado há 1 hora
        Algorithm algorithm = Algorithm.HMAC256(TEST_SECRET);
        String expiredToken = JWT.create()
                .withIssuer("login-auth-api")
                .withSubject(TEST_EMAIL)
                .withExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .sign(algorithm);

        // Act
        String result = tokenService.validateToken(expiredToken);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Deve retornar null para token com issuer errado")
    void validateToken_withWrongIssuer_shouldReturnNull() {
        // Arrange
        Algorithm algorithm = Algorithm.HMAC256(TEST_SECRET);
        String tokenWrongIssuer = JWT.create()
                .withIssuer("wrong-issuer")
                .withSubject(TEST_EMAIL)
                .withExpiresAt(Instant.now().plus(2, ChronoUnit.HOURS))
                .sign(algorithm);

        // Act
        String result = tokenService.validateToken(tokenWrongIssuer);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Deve retornar null para token assinado com chave diferente")
    void validateToken_withDifferentSecret_shouldReturnNull() {
        // Arrange
        String differentSecret = "different-secret-key-123456789";
        Algorithm algorithm = Algorithm.HMAC256(differentSecret);
        String tokenDifferentSecret = JWT.create()
                .withIssuer("login-auth-api")
                .withSubject(TEST_EMAIL)
                .withExpiresAt(Instant.now().plus(2, ChronoUnit.HOURS))
                .sign(algorithm);

        // Act
        String result = tokenService.validateToken(tokenDifferentSecret);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Deve retornar null para token null")
    void validateToken_withNullToken_shouldReturnNull() {
        // Act
        String result = tokenService.validateToken(null);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Deve retornar null para token vazio")
    void validateToken_withEmptyToken_shouldReturnNull() {
        // Act
        String result = tokenService.validateToken("");

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Deve validar token com email diferente")
    void validateToken_withDifferentEmail_shouldReturnCorrectEmail() {
        // Arrange
        String anotherEmail = "another@example.com";
        Algorithm algorithm = Algorithm.HMAC256(TEST_SECRET);
        String token = JWT.create()
                .withIssuer("login-auth-api")
                .withSubject(anotherEmail)
                .withExpiresAt(Instant.now().plus(2, ChronoUnit.HOURS))
                .sign(algorithm);

        // Act
        String result = tokenService.validateToken(token);

        // Assert
        assertEquals(anotherEmail, result);
    }

    @Test
    @DisplayName("Deve validar token próximo da expiração")
    void validateToken_nearExpiration_shouldStillBeValid() {
        // Arrange - token expira em 1 minuto
        Algorithm algorithm = Algorithm.HMAC256(TEST_SECRET);
        String token = JWT.create()
                .withIssuer("login-auth-api")
                .withSubject(TEST_EMAIL)
                .withExpiresAt(Instant.now().plus(1, ChronoUnit.MINUTES))
                .sign(algorithm);

        // Act
        String result = tokenService.validateToken(token);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_EMAIL, result);
    }
}

