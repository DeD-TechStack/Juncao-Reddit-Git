package com.redgit.auth.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.redgit.auth.infrastructure.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @InjectMocks
    private TokenService tokenService;

    private static final String TEST_SECRET = "my-test-secret-key-123456789012345678901234567890";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_ISSUER = "login-auth-api";


    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tokenService, "secret", TEST_SECRET);
    }

    @Test
    @DisplayName("Deve gerar um token JWT válido para o usuário")
    void generateToken_shouldReturnValidToken() {
        // Arrange
        User testUser = new User();
        testUser.setEmail(TEST_EMAIL);

        // Act
        String token = tokenService.generateToken(testUser);

        // Assert
        assertNotNull(token, "O token não deve ser nulo");
        assertFalse(token.isEmpty(), "O token não deve ser vazio");

        String subject = tokenService.validateToken(token);
        assertEquals(TEST_EMAIL, subject, "O subject do token deve ser o email do usuário");
    }

    @Test
    @DisplayName("Deve validar um token válido e retornar o email do usuário")
    void validateToken_shouldReturnSubjectForValidToken() {
        // Arrange
        User testUser = new User();
        testUser.setEmail(TEST_EMAIL);
        String validToken = tokenService.generateToken(testUser);

        // Act
        String subject = tokenService.validateToken(validToken);

        // Assert
        assertEquals(TEST_EMAIL, subject, "O subject retornado deve ser o email do usuário");
    }

    @Test
    @DisplayName("Deve retornar null para um token inválido")
    void validateToken_shouldReturnNullForInvalidToken() {
        // Arrange
        String invalidToken = "token.invalido.qualquercoisa";

        // Act
        String subject = tokenService.validateToken(invalidToken);

        // Assert
        assertNull(subject, "Deve retornar nulo para um token inválido");
    }

    @Test
    @DisplayName("Deve retornar null para um token assinado com uma chave secreta diferente")
    void validateToken_shouldReturnNullForTokenSignedWithDifferentSecret() {
        // Arrange
        String otherSecret = "another-test-secret-key-987654321098765432109876543210";
        Algorithm algorithm = Algorithm.HMAC256(otherSecret);
        String forgedToken = JWT.create()
                .withIssuer(TEST_ISSUER)
                .withSubject(TEST_EMAIL)
                .withExpiresAt(Instant.now().plusSeconds(3600)) // 1 hora de validade
                .sign(algorithm);

        // Act
        String subject = tokenService.validateToken(forgedToken);

        // Assert
        assertNull(subject, "Deve retornar nulo para um token assinado com chave diferente");
    }

    @Test
    @DisplayName("Deve gerar data de expiração para 2 horas no fuso -03:00")
    void generateExpirationDate_shouldBeTwoHoursFromNowInCorrectZone() {
        // Arrange
        Instant expirationDate = ReflectionTestUtils.invokeMethod(tokenService, "generateExpirationDate");
        Instant now = LocalDateTime.now().toInstant(ZoneOffset.of("-03:00"));
        Instant expectedExpiration = now.plusSeconds(7200); // 2 horas = 7200 segundos

        // Act & Assert
        assertNotNull(expirationDate, "A data de expiração não deve ser nula");

        assertTrue(expirationDate.isAfter(now.plusSeconds(7199)), "A data de expiração deve ser aproximadamente 2 horas no futuro");
        assertTrue(expirationDate.isBefore(now.plusSeconds(7201)), "A data de expiração deve ser aproximadamente 2 horas no futuro");
    }
}
