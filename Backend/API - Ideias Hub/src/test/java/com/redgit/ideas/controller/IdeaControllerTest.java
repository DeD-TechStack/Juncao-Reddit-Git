package com.redgit.ideas.controller;

import com.redgit.ideas.controller.dto.IdeaCreateDTO;
import com.redgit.ideas.controller.dto.IdeaDTO;
import com.redgit.ideas.infrastructure.entities.Idea;
import com.redgit.ideas.service.IdeaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IdeaController Tests")
class IdeaControllerTest {

    @Mock
    private IdeaService ideaService;

    @InjectMocks
    private IdeaController ideaController;

    private Idea testIdea;

    @BeforeEach
    void setUp() {
        testIdea = new Idea();
        testIdea.setId("test-id-123");
        testIdea.setTitle("Test Idea");
        testIdea.setDescription("Test Description");
        testIdea.setAuthorId("user@test.com");
        testIdea.setLikesCount(0L);
        testIdea.setCreatedAt(LocalDateTime.now());
    }

    // ========== likeIdea Tests ==========

    @Nested
    @DisplayName("likeIdea Tests")
    class LikeIdeaTests {

        @Test
        @DisplayName("Deve retornar 200 com ideia atualizada ao curtir")
        void likeIdea_shouldReturn200WithUpdatedIdea() {
            // Arrange
            Idea liked = new Idea();
            liked.setId("test-id-123");
            liked.setLikesCount(1L);
            when(ideaService.likeIdea("test-id-123")).thenReturn(liked);

            // Act
            ResponseEntity<Idea> response = ideaController.likeIdea("test-id-123", "user@test.com");

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1L, response.getBody().getLikesCount());
            verify(ideaService, times(1)).likeIdea("test-id-123");
        }

        @Test
        @DisplayName("Deve chamar ideaService.likeIdea com o ID correto")
        void likeIdea_shouldCallServiceWithCorrectId() {
            // Arrange
            when(ideaService.likeIdea("test-id-123")).thenReturn(testIdea);

            // Act
            ideaController.likeIdea("test-id-123", "user@test.com");

            // Assert
            verify(ideaService, times(1)).likeIdea("test-id-123");
            verify(ideaService, never()).likeIdea("outro-id");
        }

        @Test
        @DisplayName("Deve propagar exceção 404 quando ideia não existe")
        void likeIdea_whenIdeaNotFound_shouldThrowException() {
            // Arrange
            when(ideaService.likeIdea("non-existent")).thenThrow(
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "Ideia não encontrada: non-existent"));

            // Act & Assert
            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> ideaController.likeIdea("non-existent", "user@test.com"));

            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
            verify(ideaService, times(1)).likeIdea("non-existent");
        }

        @Test
        @DisplayName("Deve aceitar userEmail nulo (endpoint será protegido pelo SecurityFilter)")
        void likeIdea_withNullUserEmail_shouldStillCallService() {
            // Arrange — sem token, userEmail seria null; a rejeição ocorre no filtro, não no controller
            when(ideaService.likeIdea("test-id-123")).thenReturn(testIdea);

            // Act
            ResponseEntity<Idea> response = ideaController.likeIdea("test-id-123", null);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(ideaService, times(1)).likeIdea("test-id-123");
        }
    }

    // ========== createIdea Tests ==========

    @Nested
    @DisplayName("createIdea Tests")
    class CreateIdeaTests {

        @Test
        @DisplayName("Deve retornar 201 ao criar ideia")
        void createIdea_shouldReturn201() {
            // Arrange
            IdeaCreateDTO dto = new IdeaCreateDTO("Nova Ideia", "Descrição da ideia");
            when(ideaService.createIdea(any(IdeaCreateDTO.class), eq("user@test.com"))).thenReturn(testIdea);

            // Act
            ResponseEntity<Idea> response = ideaController.createIdea(dto, "user@test.com");

            // Assert
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(ideaService, times(1)).createIdea(any(IdeaCreateDTO.class), eq("user@test.com"));
        }
    }

    // ========== getAllIdeas Tests ==========

    @Nested
    @DisplayName("getAllIdeas Tests")
    class GetAllIdeasTests {

        @Test
        @DisplayName("Deve retornar 200 com página de ideias")
        void getAllIdeas_shouldReturn200WithPage() {
            // Arrange
            Page<Idea> page = new PageImpl<>(List.of(testIdea));
            when(ideaService.getAllIdeas(any(Pageable.class))).thenReturn(page);

            // Act
            ResponseEntity<Page<Idea>> response = ideaController.getAllIdeas(Pageable.unpaged());

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getTotalElements());
        }
    }

    // ========== getIdeaById Tests ==========

    @Nested
    @DisplayName("getIdeaById Tests")
    class GetIdeaByIdTests {

        @Test
        @DisplayName("Deve retornar 200 com ideia quando existe")
        void getIdeaById_whenExists_shouldReturn200() {
            // Arrange
            when(ideaService.findById("test-id-123")).thenReturn(testIdea);

            // Act
            ResponseEntity<Idea> response = ideaController.getIdeaById("test-id-123");

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("test-id-123", response.getBody().getId());
        }

        @Test
        @DisplayName("Deve propagar exceção 404 quando ideia não existe")
        void getIdeaById_whenNotExists_shouldThrow404() {
            // Arrange
            when(ideaService.findById("non-existent")).thenThrow(
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "Ideia não encontrada: non-existent"));

            // Act & Assert
            assertThrows(ResponseStatusException.class,
                    () -> ideaController.getIdeaById("non-existent"));
        }
    }

    // ========== deleteIdea Tests ==========

    @Nested
    @DisplayName("deleteIdea Tests")
    class DeleteIdeaTests {

        @Test
        @DisplayName("Deve retornar 204 ao deletar ideia própria")
        void deleteIdea_byAuthor_shouldReturn204() {
            // Arrange
            when(ideaService.findById("test-id-123")).thenReturn(testIdea);
            doNothing().when(ideaService).deleteIdeaById("test-id-123");

            // Act
            ResponseEntity<Void> response = ideaController.deleteIdea("test-id-123", "user@test.com");

            // Assert
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            verify(ideaService, times(1)).deleteIdeaById("test-id-123");
        }

        @Test
        @DisplayName("Deve retornar 403 ao tentar deletar ideia de outro usuário")
        void deleteIdea_byNonAuthor_shouldReturn403() {
            // Arrange
            when(ideaService.findById("test-id-123")).thenReturn(testIdea); // authorId = user@test.com

            // Act
            ResponseEntity<Void> response = ideaController.deleteIdea("test-id-123", "outro@test.com");

            // Assert
            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
            verify(ideaService, never()).deleteIdeaById(any());
        }
    }
}
