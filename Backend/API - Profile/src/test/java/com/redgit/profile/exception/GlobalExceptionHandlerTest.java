package com.redgit.profile.exception;

import com.redgit.profile.infrastructure.exception.ErrorResponse;
import com.redgit.profile.infrastructure.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    // ========== Validation Errors Tests ==========

    @Test
    @DisplayName("Deve tratar erros de validação e retornar 400")
    void handleValidationErrors_shouldReturn400WithFieldErrors() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("profile", "username", "Username é obrigatório");
        FieldError fieldError2 = new FieldError("profile", "email", "Email inválido");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationErrors(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("Erro de validação");

        @SuppressWarnings("unchecked")
        Map<String, String> details = (Map<String, String>) response.getBody().getDetails();
        assertThat(details).hasSize(2);
        assertThat(details.get("username")).isEqualTo("Username é obrigatório");
        assertThat(details.get("email")).isEqualTo("Email inválido");
    }

    @Test
    @DisplayName("Deve incluir timestamp no erro de validação")
    void handleValidationErrors_shouldIncludeTimestamp() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of());

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationErrors(exception);

        // Assert
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    // ========== File Upload Errors Tests ==========

    @Test
    @DisplayName("Deve tratar erro de arquivo muito grande e retornar 400")
    void handleMaxSizeException_shouldReturn400() {
        // Arrange
        MaxUploadSizeExceededException exception = new MaxUploadSizeExceededException(2 * 1024 * 1024);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMaxSizeException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("Arquivo muito grande. Tamanho máximo: 2MB");
        assertThat(response.getBody().getDetails()).isNull();
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    // ========== Authentication Errors Tests ==========

    @Test
    @DisplayName("Deve tratar BadCredentialsException e retornar 401")
    void handleBadCredentials_shouldReturn401() {
        // Arrange
        BadCredentialsException exception = new BadCredentialsException("Invalid credentials");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadCredentials(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(401);
        assertThat(response.getBody().getMessage()).isEqualTo("Credenciais inválidas");
        assertThat(response.getBody().getDetails()).isNull();
    }

    @Test
    @DisplayName("Deve tratar AuthenticationException genérica e retornar 401")
    void handleAuthenticationException_shouldReturn401() {
        // Arrange
        AuthenticationException exception = new AuthenticationException("Authentication failed") {};

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuthenticationException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(401);
        assertThat(response.getBody().getMessage()).isEqualTo("Falha na autenticação");
    }

    // ========== Authorization Errors Tests ==========

    @Test
    @DisplayName("Deve tratar AccessDeniedException e retornar 403")
    void handleAccessDenied_shouldReturn403() {
        // Arrange
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDenied(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(403);
        assertThat(response.getBody().getMessage()).isEqualTo("Acesso negado");
        assertThat(response.getBody().getDetails()).isNull();
    }

    // ========== ResponseStatusException Tests ==========

    @Test
    @DisplayName("Deve tratar ResponseStatusException 404 com mensagem customizada")
    void handleResponseStatusException_with404_shouldReturnCorrectMessage() {
        // Arrange
        ResponseStatusException exception = new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Perfil não encontrado"
        );

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResponseStatusException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).isEqualTo("Perfil não encontrado");
    }

    @Test
    @DisplayName("Deve tratar ResponseStatusException 409 (Conflict)")
    void handleResponseStatusException_with409_shouldReturnConflict() {
        // Arrange
        ResponseStatusException exception = new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Username já está em uso"
        );

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResponseStatusException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getMessage()).isEqualTo("Username já está em uso");
    }

    @Test
    @DisplayName("Deve usar mensagem padrão quando reason é null")
    void handleResponseStatusException_withNullReason_shouldUseDefaultMessage() {
        // Arrange
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.BAD_REQUEST);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResponseStatusException(exception);

        // Assert
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Erro");
    }

    // ========== IllegalArgumentException Tests ==========

    @Test
    @DisplayName("Deve tratar IllegalArgumentException e retornar 400")
    void handleIllegalArgument_shouldReturn400() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Argumento inválido");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgument(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("Argumento inválido");
    }

    @Test
    @DisplayName("Deve usar mensagem padrão quando IllegalArgumentException não tem mensagem")
    void handleIllegalArgument_withNullMessage_shouldUseDefault() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException();

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgument(exception);

        // Assert
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Argumento inválido");
    }

    // ========== IllegalStateException Tests ==========

    @Test
    @DisplayName("Deve tratar IllegalStateException e retornar 400")
    void handleIllegalState_shouldReturn400() {
        // Arrange
        IllegalStateException exception = new IllegalStateException("Estado inválido");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalState(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("Estado inválido");
    }

    // ========== Generic Exception Tests ==========

    @Test
    @DisplayName("Deve tratar Exception genérica e retornar 500")
    void handleGenericException_shouldReturn500() {
        // Arrange
        Exception exception = new Exception("Erro inesperado");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).isEqualTo("Erro interno do servidor");
        assertThat(response.getBody().getDetails()).isNull(); // Não expõe detalhes internos
    }

    @Test
    @DisplayName("Deve incluir timestamp em todas as respostas de erro")
    void allHandlers_shouldIncludeTimestamp() {
        // Act
        ResponseEntity<ErrorResponse> response1 = exceptionHandler.handleBadCredentials(
                new BadCredentialsException("test")
        );
        ResponseEntity<ErrorResponse> response2 = exceptionHandler.handleAccessDenied(
                new AccessDeniedException("test")
        );
        ResponseEntity<ErrorResponse> response3 = exceptionHandler.handleIllegalArgument(
                new IllegalArgumentException("test")
        );

        // Assert
        assertThat(response1.getBody().getTimestamp()).isNotNull();
        assertThat(response2.getBody().getTimestamp()).isNotNull();
        assertThat(response3.getBody().getTimestamp()).isNotNull();
    }

    // ========== Error Response Structure Tests ==========

    @Test
    @DisplayName("Deve criar ErrorResponse com estrutura completa")
    void errorResponse_shouldHaveCompleteStructure() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Test error");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgument(exception);

        // Assert
        ErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatus()).isNotNull();
        assertThat(errorResponse.getMessage()).isNotNull();
        assertThat(errorResponse.getTimestamp()).isNotNull();
        // details pode ser null
    }

    @Test
    @DisplayName("Deve manter details null quando não há detalhes adicionais")
    void errorResponse_shouldKeepDetailsNullWhenNotNeeded() {
        // Arrange
        BadCredentialsException exception = new BadCredentialsException("test");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadCredentials(exception);

        // Assert
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetails()).isNull();
    }

    @Test
    @DisplayName("Deve incluir details apenas em erros de validação")
    void errorResponse_shouldIncludeDetailsOnlyForValidation() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError = new FieldError("profile", "username", "Required");
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationErrors(exception);

        // Assert
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetails()).isNotNull();
        assertThat(response.getBody().getDetails()).isInstanceOf(Map.class);
    }

    // ========== HTTP Status Code Tests ==========

    @Test
    @DisplayName("Deve retornar códigos HTTP corretos para cada tipo de erro")
    void handlers_shouldReturnCorrectHttpStatus() {
        // Arrange & Act
        var response400 = exceptionHandler.handleIllegalArgument(new IllegalArgumentException("test"));
        var response401 = exceptionHandler.handleBadCredentials(new BadCredentialsException("test"));
        var response403 = exceptionHandler.handleAccessDenied(new AccessDeniedException("test"));
        var response404 = exceptionHandler.handleResponseStatusException(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found")
        );
        var response500 = exceptionHandler.handleGenericException(new Exception("test"));

        // Assert
        assertThat(response400.getStatusCode().value()).isEqualTo(400);
        assertThat(response401.getStatusCode().value()).isEqualTo(401);
        assertThat(response403.getStatusCode().value()).isEqualTo(403);
        assertThat(response404.getStatusCode().value()).isEqualTo(404);
        assertThat(response500.getStatusCode().value()).isEqualTo(500);
    }
}
