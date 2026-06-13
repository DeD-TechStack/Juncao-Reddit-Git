package com.redgit.auth.exception;

import com.redgit.auth.infrastructure.exception.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ErrorResponse Tests")
class ErrorResponseTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Deve criar ErrorResponse com todos os campos")
        void constructor_withAllFields_shouldCreateObject() {
            // Arrange
            int status = 400;
            String message = "Erro de validação";
            Map<String, String> details = new HashMap<>();
            details.put("field", "error");
            LocalDateTime timestamp = LocalDateTime.now();

            // Act
            ErrorResponse errorResponse = new ErrorResponse(status, message, details, timestamp);

            // Assert
            assertNotNull(errorResponse);
            assertEquals(status, errorResponse.getStatus());
            assertEquals(message, errorResponse.getMessage());
            assertEquals(details, errorResponse.getDetails());
            assertEquals(timestamp, errorResponse.getTimestamp());
        }

        @Test
        @DisplayName("Deve criar ErrorResponse com details null")
        void constructor_withNullDetails_shouldCreateObject() {
            // Arrange
            int status = 401;
            String message = "Não autorizado";
            LocalDateTime timestamp = LocalDateTime.now();

            // Act
            ErrorResponse errorResponse = new ErrorResponse(status, message, null, timestamp);

            // Assert
            assertNotNull(errorResponse);
            assertEquals(status, errorResponse.getStatus());
            assertEquals(message, errorResponse.getMessage());
            assertNull(errorResponse.getDetails());
            assertEquals(timestamp, errorResponse.getTimestamp());
        }
    }

    @Nested
    @DisplayName("Getters and Setters Tests")
    class GettersSettersTests {

        @Test
        @DisplayName("Deve obter e definir status corretamente")
        void getSetStatus_shouldWorkCorrectly() {
            // Arrange
            ErrorResponse errorResponse = new ErrorResponse(0, "", null, LocalDateTime.now());

            // Act
            errorResponse.setStatus(404);

            // Assert
            assertEquals(404, errorResponse.getStatus());
        }

        @Test
        @DisplayName("Deve obter e definir message corretamente")
        void getSetMessage_shouldWorkCorrectly() {
            // Arrange
            ErrorResponse errorResponse = new ErrorResponse(0, "", null, LocalDateTime.now());

            // Act
            errorResponse.setMessage("Recurso não encontrado");

            // Assert
            assertEquals("Recurso não encontrado", errorResponse.getMessage());
        }

        @Test
        @DisplayName("Deve obter e definir details corretamente")
        void getSetDetails_shouldWorkCorrectly() {
            // Arrange
            ErrorResponse errorResponse = new ErrorResponse(0, "", null, LocalDateTime.now());
            Map<String, String> details = new HashMap<>();
            details.put("error", "validation failed");

            // Act
            errorResponse.setDetails(details);

            // Assert
            assertEquals(details, errorResponse.getDetails());
        }

        @Test
        @DisplayName("Deve obter e definir timestamp corretamente")
        void getSetTimestamp_shouldWorkCorrectly() {
            // Arrange
            ErrorResponse errorResponse = new ErrorResponse(0, "", null, LocalDateTime.now());
            LocalDateTime newTimestamp = LocalDateTime.of(2024, 12, 1, 10, 30);

            // Act
            errorResponse.setTimestamp(newTimestamp);

            // Assert
            assertEquals(newTimestamp, errorResponse.getTimestamp());
        }
    }

    @Nested
    @DisplayName("Status Code Tests")
    class StatusCodeTests {

        @Test
        @DisplayName("Deve aceitar código 400 Bad Request")
        void status_withBadRequest_shouldBe400() {
            // Arrange & Act
            ErrorResponse errorResponse = new ErrorResponse(400, "Bad Request", null, LocalDateTime.now());

            // Assert
            assertEquals(400, errorResponse.getStatus());
        }

        @Test
        @DisplayName("Deve aceitar código 401 Unauthorized")
        void status_withUnauthorized_shouldBe401() {
            // Arrange & Act
            ErrorResponse errorResponse = new ErrorResponse(401, "Unauthorized", null, LocalDateTime.now());

            // Assert
            assertEquals(401, errorResponse.getStatus());
        }

        @Test
        @DisplayName("Deve aceitar código 403 Forbidden")
        void status_withForbidden_shouldBe403() {
            // Arrange & Act
            ErrorResponse errorResponse = new ErrorResponse(403, "Forbidden", null, LocalDateTime.now());

            // Assert
            assertEquals(403, errorResponse.getStatus());
        }

        @Test
        @DisplayName("Deve aceitar código 404 Not Found")
        void status_withNotFound_shouldBe404() {
            // Arrange & Act
            ErrorResponse errorResponse = new ErrorResponse(404, "Not Found", null, LocalDateTime.now());

            // Assert
            assertEquals(404, errorResponse.getStatus());
        }

        @Test
        @DisplayName("Deve aceitar código 500 Internal Server Error")
        void status_withInternalServerError_shouldBe500() {
            // Arrange & Act
            ErrorResponse errorResponse = new ErrorResponse(500, "Internal Server Error", null, LocalDateTime.now());

            // Assert
            assertEquals(500, errorResponse.getStatus());
        }
    }

    @Nested
    @DisplayName("Details Tests")
    class DetailsTests {

        @Test
        @DisplayName("Deve aceitar details como Map de Strings")
        void details_withStringMap_shouldWork() {
            // Arrange
            Map<String, String> details = new HashMap<>();
            details.put("field1", "error1");
            details.put("field2", "error2");

            // Act
            ErrorResponse errorResponse = new ErrorResponse(400, "Validation Error", details, LocalDateTime.now());

            // Assert
            assertNotNull(errorResponse.getDetails());
            assertEquals(2, ((Map<?, ?>) errorResponse.getDetails()).size());
        }

        @Test
        @DisplayName("Deve aceitar details como Object genérico")
        void details_withGenericObject_shouldWork() {
            // Arrange
            String detailsString = "Simple error message";

            // Act
            ErrorResponse errorResponse = new ErrorResponse(400, "Error", detailsString, LocalDateTime.now());

            // Assert
            assertNotNull(errorResponse.getDetails());
            assertEquals("Simple error message", errorResponse.getDetails());
        }

        @Test
        @DisplayName("Deve aceitar details null")
        void details_withNull_shouldWork() {
            // Arrange & Act
            ErrorResponse errorResponse = new ErrorResponse(401, "Unauthorized", null, LocalDateTime.now());

            // Assert
            assertNull(errorResponse.getDetails());
        }

        @Test
        @DisplayName("Deve permitir atualizar details")
        void details_shouldBeUpdatable() {
            // Arrange
            ErrorResponse errorResponse = new ErrorResponse(400, "Error", null, LocalDateTime.now());
            Map<String, String> newDetails = new HashMap<>();
            newDetails.put("newField", "newError");

            // Act
            errorResponse.setDetails(newDetails);

            // Assert
            assertNotNull(errorResponse.getDetails());
            assertEquals(newDetails, errorResponse.getDetails());
        }
    }

    @Nested
    @DisplayName("Timestamp Tests")
    class TimestampTests {

        @Test
        @DisplayName("Deve armazenar timestamp preciso")
        void timestamp_shouldStorePreciseTime() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();

            // Act
            ErrorResponse errorResponse = new ErrorResponse(400, "Error", null, now);

            // Assert
            assertEquals(now, errorResponse.getTimestamp());
        }

        @Test
        @DisplayName("Deve aceitar timestamp no passado")
        void timestamp_withPastDate_shouldWork() {
            // Arrange
            LocalDateTime pastDate = LocalDateTime.of(2023, 1, 1, 12, 0);

            // Act
            ErrorResponse errorResponse = new ErrorResponse(400, "Error", null, pastDate);

            // Assert
            assertEquals(pastDate, errorResponse.getTimestamp());
        }

        @Test
        @DisplayName("Deve aceitar timestamp no futuro")
        void timestamp_withFutureDate_shouldWork() {
            // Arrange
            LocalDateTime futureDate = LocalDateTime.of(2025, 12, 31, 23, 59);

            // Act
            ErrorResponse errorResponse = new ErrorResponse(400, "Error", null, futureDate);

            // Assert
            assertEquals(futureDate, errorResponse.getTimestamp());
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Deve criar response completo de erro de validação")
        void createValidationErrorResponse_shouldHaveAllFields() {
            // Arrange
            Map<String, String> validationErrors = new HashMap<>();
            validationErrors.put("title", "Title is mandatory");
            validationErrors.put("description", "Description is mandatory");
            LocalDateTime timestamp = LocalDateTime.now();

            // Act
            ErrorResponse errorResponse = new ErrorResponse(
                    400,
                    "Erro de validação",
                    validationErrors,
                    timestamp
            );

            // Assert
            assertEquals(400, errorResponse.getStatus());
            assertEquals("Erro de validação", errorResponse.getMessage());
            assertNotNull(errorResponse.getDetails());

            @SuppressWarnings("unchecked")
            Map<String, String> details = (Map<String, String>) errorResponse.getDetails();
            assertEquals(2, details.size());
            assertTrue(details.containsKey("title"));
            assertTrue(details.containsKey("description"));
            assertEquals(timestamp, errorResponse.getTimestamp());
        }

        @Test
        @DisplayName("Deve criar response de erro de autenticação")
        void createAuthenticationErrorResponse_shouldHaveCorrectStructure() {
            // Arrange
            LocalDateTime timestamp = LocalDateTime.now();

            // Act
            ErrorResponse errorResponse = new ErrorResponse(
                    401,
                    "Falha na autenticação",
                    null,
                    timestamp
            );

            // Assert
            assertEquals(401, errorResponse.getStatus());
            assertEquals("Falha na autenticação", errorResponse.getMessage());
            assertNull(errorResponse.getDetails());
            assertEquals(timestamp, errorResponse.getTimestamp());
        }

        @Test
        @DisplayName("Deve criar response de erro de autorização")
        void createAuthorizationErrorResponse_shouldHaveCorrectStructure() {
            // Arrange
            LocalDateTime timestamp = LocalDateTime.now();

            // Act
            ErrorResponse errorResponse = new ErrorResponse(
                    403,
                    "Acesso negado",
                    null,
                    timestamp
            );

            // Assert
            assertEquals(403, errorResponse.getStatus());
            assertEquals("Acesso negado", errorResponse.getMessage());
            assertNull(errorResponse.getDetails());
            assertEquals(timestamp, errorResponse.getTimestamp());
        }

        @Test
        @DisplayName("Deve criar response de erro interno do servidor")
        void createServerErrorResponse_shouldHaveCorrectStructure() {
            // Arrange
            LocalDateTime timestamp = LocalDateTime.now();

            // Act
            ErrorResponse errorResponse = new ErrorResponse(
                    500,
                    "Erro interno do servidor",
                    null,
                    timestamp
            );

            // Assert
            assertEquals(500, errorResponse.getStatus());
            assertEquals("Erro interno do servidor", errorResponse.getMessage());
            assertNull(errorResponse.getDetails());
            assertEquals(timestamp, errorResponse.getTimestamp());
        }
    }

    @Nested
    @DisplayName("Lombok Tests")
    class LombokTests {

        @Test
        @DisplayName("Deve ter toString funcionando corretamente via Lombok @Data")
        void toString_shouldWork() {
            // Arrange
            ErrorResponse errorResponse = new ErrorResponse(
                    400,
                    "Test Error",
                    null,
                    LocalDateTime.of(2024, 12, 1, 10, 0)
            );

            // Act
            String toString = errorResponse.toString();

            // Assert
            assertNotNull(toString);
            assertTrue(toString.contains("400"));
            assertTrue(toString.contains("Test Error"));
        }

        @Test
        @DisplayName("Deve ter equals funcionando corretamente via Lombok @Data")
        void equals_shouldWork() {
            // Arrange
            LocalDateTime timestamp = LocalDateTime.of(2024, 12, 1, 10, 0);
            ErrorResponse errorResponse1 = new ErrorResponse(400, "Error", null, timestamp);
            ErrorResponse errorResponse2 = new ErrorResponse(400, "Error", null, timestamp);

            // Act & Assert
            assertEquals(errorResponse1, errorResponse2);
        }

        @Test
        @DisplayName("Deve ter hashCode funcionando corretamente via Lombok @Data")
        void hashCode_shouldWork() {
            // Arrange
            LocalDateTime timestamp = LocalDateTime.of(2024, 12, 1, 10, 0);
            ErrorResponse errorResponse1 = new ErrorResponse(400, "Error", null, timestamp);
            ErrorResponse errorResponse2 = new ErrorResponse(400, "Error", null, timestamp);

            // Act & Assert
            assertEquals(errorResponse1.hashCode(), errorResponse2.hashCode());
        }
    }
}
