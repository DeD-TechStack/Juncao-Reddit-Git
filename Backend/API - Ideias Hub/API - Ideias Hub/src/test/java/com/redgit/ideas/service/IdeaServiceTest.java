package com.redgit.ideas.service;

import com.redgit.ideas.controller.dto.IdeaDTO;
import com.redgit.ideas.infrastructure.entities.Idea;
import com.redgit.ideas.infrastructure.repository.IdeaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IdeaService Tests")
class IdeaServiceTest {

    @Mock
    private IdeaRepository ideaRepository;

    @InjectMocks
    private IdeaService ideaService;

    private IdeaDTO testDTO;
    private Idea testIdea;

    @BeforeEach
    void setUp() {
        testDTO = new IdeaDTO();
        testDTO.setTitle("Test Idea");
        testDTO.setDescription("Test Description");
        testDTO.setAuthorId("user@test.com");

        testIdea = new Idea();
        testIdea.setId("test-id-123");
        testIdea.setTitle("Test Idea");
        testIdea.setDescription("Test Description");
        testIdea.setAuthorId("user@test.com");
        testIdea.setCreatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("createIdea Tests")
    class CreateIdeaTests {

        @Test
        @DisplayName("Deve criar ideia com sucesso")
        void createIdea_withValidData_shouldReturnSavedIdea() {
            // Arrange
            when(ideaRepository.save(any(Idea.class))).thenReturn(testIdea);

            // Act
            Idea result = ideaService.createIdea(testDTO);

            // Assert
            assertNotNull(result);
            assertEquals("Test Idea", result.getTitle());
            assertEquals("Test Description", result.getDescription());
            assertEquals("user@test.com", result.getAuthorId());
            verify(ideaRepository, times(1)).save(any(Idea.class));
        }

        @Test
        @DisplayName("Deve configurar createdAt ao criar ideia")
        void createIdea_shouldSetCreatedAt() {
            // Arrange
            ArgumentCaptor<Idea> ideaCaptor = ArgumentCaptor.forClass(Idea.class);
            when(ideaRepository.save(any(Idea.class))).thenReturn(testIdea);

            // Act
            ideaService.createIdea(testDTO);

            // Assert
            verify(ideaRepository).save(ideaCaptor.capture());
            Idea captured = ideaCaptor.getValue();
            assertNotNull(captured.getCreatedAt());
        }

        @Test
        @DisplayName("Deve mapear todos os campos do DTO para a entidade")
        void createIdea_shouldMapAllFields() {
            // Arrange
            ArgumentCaptor<Idea> ideaCaptor = ArgumentCaptor.forClass(Idea.class);
            when(ideaRepository.save(any(Idea.class))).thenReturn(testIdea);

            // Act
            ideaService.createIdea(testDTO);

            // Assert
            verify(ideaRepository).save(ideaCaptor.capture());
            Idea captured = ideaCaptor.getValue();
            assertEquals(testDTO.getTitle(), captured.getTitle());
            assertEquals(testDTO.getDescription(), captured.getDescription());
            assertEquals(testDTO.getAuthorId(), captured.getAuthorId());
        }

        @Test
        @DisplayName("Deve criar nova instância de Idea")
        void createIdea_shouldCreateNewIdeaInstance() {
            // Arrange
            ArgumentCaptor<Idea> ideaCaptor = ArgumentCaptor.forClass(Idea.class);
            when(ideaRepository.save(any(Idea.class))).thenReturn(testIdea);

            // Act
            ideaService.createIdea(testDTO);

            // Assert
            verify(ideaRepository).save(ideaCaptor.capture());
            Idea captured = ideaCaptor.getValue();
            assertNotNull(captured);
        }
    }

    @Nested
    @DisplayName("getAllIdeas Tests")
    class GetAllIdeasTests {

        @Test
        @DisplayName("Deve retornar todas as ideias")
        void getAllIdeas_shouldReturnAllIdeas() {
            // Arrange
            Idea idea2 = new Idea();
            idea2.setId("test-id-456");
            idea2.setTitle("Another Idea");
            idea2.setDescription("Another Description");
            idea2.setAuthorId("another@test.com");

            List<Idea> ideas = Arrays.asList(testIdea, idea2);
            when(ideaRepository.findAll()).thenReturn(ideas);

            // Act
            List<Idea> result = ideaService.getAllIdeas();

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("Test Idea", result.get(0).getTitle());
            assertEquals("Another Idea", result.get(1).getTitle());
            verify(ideaRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há ideias")
        void getAllIdeas_whenEmpty_shouldReturnEmptyList() {
            // Arrange
            when(ideaRepository.findAll()).thenReturn(Arrays.asList());

            // Act
            List<Idea> result = ideaService.getAllIdeas();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(ideaRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Deve chamar repository.findAll apenas uma vez")
        void getAllIdeas_shouldCallRepositoryOnce() {
            // Arrange
            when(ideaRepository.findAll()).thenReturn(Arrays.asList(testIdea));

            // Act
            ideaService.getAllIdeas();

            // Assert
            verify(ideaRepository, times(1)).findAll();
        }
    }

    @Nested
    @DisplayName("findById Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Deve encontrar ideia por ID quando existe")
        void findById_whenExists_shouldReturnIdea() {
            // Arrange
            when(ideaRepository.findById("test-id-123")).thenReturn(Optional.of(testIdea));

            // Act
            Idea result = ideaService.findById("test-id-123");

            // Assert
            assertNotNull(result);
            assertEquals("test-id-123", result.getId());
            assertEquals("Test Idea", result.getTitle());
            verify(ideaRepository, times(1)).findById("test-id-123");
        }

        @Test
        @DisplayName("Deve lançar exceção quando ID não existe")
        void findById_whenNotExists_shouldThrowException() {
            // Arrange
            when(ideaRepository.findById("non-existent")).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> ideaService.findById("non-existent"));

            assertEquals("id não encontrado", exception.getMessage());
            verify(ideaRepository, times(1)).findById("non-existent");
        }

        @Test
        @DisplayName("Deve retornar ideia com todos os campos")
        void findById_shouldReturnIdeaWithAllFields() {
            // Arrange
            when(ideaRepository.findById("test-id-123")).thenReturn(Optional.of(testIdea));

            // Act
            Idea result = ideaService.findById("test-id-123");

            // Assert
            assertEquals("test-id-123", result.getId());
            assertEquals("Test Idea", result.getTitle());
            assertEquals("Test Description", result.getDescription());
            assertEquals("user@test.com", result.getAuthorId());
            assertNotNull(result.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("getIdeasByAuthor Tests")
    class GetIdeasByAuthorTests {

        @Test
        @DisplayName("Deve retornar todas as ideias de um autor")
        void getIdeasByAuthor_shouldReturnAuthorIdeas() {
            // Arrange
            Idea idea2 = new Idea();
            idea2.setId("test-id-456");
            idea2.setTitle("Another Idea");
            idea2.setAuthorId("user@test.com");

            List<Idea> ideas = Arrays.asList(testIdea, idea2);
            when(ideaRepository.findByAuthorId("user@test.com")).thenReturn(ideas);

            // Act
            List<Idea> result = ideaService.getIdeasByAuthor("user@test.com");

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(
                    idea -> "user@test.com".equals(idea.getAuthorId())));
            verify(ideaRepository, times(1)).findByAuthorId("user@test.com");
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando autor não tem ideias")
        void getIdeasByAuthor_whenNoIdeas_shouldReturnEmptyList() {
            // Arrange
            when(ideaRepository.findByAuthorId("another@test.com")).thenReturn(Arrays.asList());

            // Act
            List<Idea> result = ideaService.getIdeasByAuthor("another@test.com");

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(ideaRepository, times(1)).findByAuthorId("another@test.com");
        }

        @Test
        @DisplayName("Deve chamar repository com authorId correto")
        void getIdeasByAuthor_shouldCallRepositoryWithCorrectAuthorId() {
            // Arrange
            when(ideaRepository.findByAuthorId(anyString())).thenReturn(Arrays.asList());

            // Act
            ideaService.getIdeasByAuthor("specific@author.com");

            // Assert
            verify(ideaRepository).findByAuthorId("specific@author.com");
        }
    }

    @Nested
    @DisplayName("replaceIdea Tests")
    class ReplaceIdeaTests {

        @Test
        @DisplayName("Deve substituir ideia completamente")
        void replaceIdea_shouldReplaceAllFields() {
            // Arrange
            when(ideaRepository.findById("test-id-123")).thenReturn(Optional.of(testIdea));
            when(ideaRepository.save(any(Idea.class))).thenReturn(testIdea);

            IdeaDTO updateDTO = new IdeaDTO();
            updateDTO.setTitle("Updated Title");
            updateDTO.setDescription("Updated Description");

            // Act
            Idea result = ideaService.replaceIdea("test-id-123", updateDTO);

            // Assert
            assertNotNull(result);
            verify(ideaRepository, times(1)).findById("test-id-123");
            verify(ideaRepository, times(1)).save(any(Idea.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando ideia não existe")
        void replaceIdea_whenNotExists_shouldThrowException() {
            // Arrange
            when(ideaRepository.findById("non-existent")).thenReturn(Optional.empty());

            IdeaDTO updateDTO = new IdeaDTO();
            updateDTO.setTitle("Updated");
            updateDTO.setDescription("Updated");

            // Act & Assert
            assertThrows(RuntimeException.class,
                    () -> ideaService.replaceIdea("non-existent", updateDTO));

            verify(ideaRepository, times(1)).findById("non-existent");
            verify(ideaRepository, never()).save(any(Idea.class));
        }

        @Test
        @DisplayName("Deve atualizar título e descrição")
        void replaceIdea_shouldUpdateTitleAndDescription() {
            // Arrange
            ArgumentCaptor<Idea> ideaCaptor = ArgumentCaptor.forClass(Idea.class);
            when(ideaRepository.findById("test-id-123")).thenReturn(Optional.of(testIdea));
            when(ideaRepository.save(any(Idea.class))).thenReturn(testIdea);

            IdeaDTO updateDTO = new IdeaDTO();
            updateDTO.setTitle("New Title");
            updateDTO.setDescription("New Description");

            // Act
            ideaService.replaceIdea("test-id-123", updateDTO);

            // Assert
            verify(ideaRepository).save(ideaCaptor.capture());
            Idea captured = ideaCaptor.getValue();
            assertEquals("New Title", captured.getTitle());
            assertEquals("New Description", captured.getDescription());
        }

        @Test
        @DisplayName("Deve manter o mesmo ID ao substituir")
        void replaceIdea_shouldKeepSameId() {
            // Arrange
            ArgumentCaptor<Idea> ideaCaptor = ArgumentCaptor.forClass(Idea.class);
            when(ideaRepository.findById("test-id-123")).thenReturn(Optional.of(testIdea));
            when(ideaRepository.save(any(Idea.class))).thenReturn(testIdea);

            IdeaDTO updateDTO = new IdeaDTO();
            updateDTO.setTitle("New Title");
            updateDTO.setDescription("New Description");

            // Act
            ideaService.replaceIdea("test-id-123", updateDTO);

            // Assert
            verify(ideaRepository).save(ideaCaptor.capture());
            Idea captured = ideaCaptor.getValue();
            assertEquals("test-id-123", captured.getId());
        }
    }

    @Nested
    @DisplayName("updateIdea Tests")
    class UpdateIdeaTests {

        @Test
        @DisplayName("Deve atualizar apenas título quando fornecido")
        void updateIdea_withOnlyTitle_shouldUpdateOnlyTitle() {
            // Arrange
            ArgumentCaptor<Idea> ideaCaptor = ArgumentCaptor.forClass(Idea.class);
            when(ideaRepository.findById("test-id-123")).thenReturn(Optional.of(testIdea));
            when(ideaRepository.save(any(Idea.class))).thenReturn(testIdea);

            IdeaDTO partialDTO = new IdeaDTO();
            partialDTO.setTitle("Only Title Updated");
            partialDTO.setDescription(null);

            String originalDescription = testIdea.getDescription();

            // Act
            ideaService.updateIdea("test-id-123", partialDTO);

            // Assert
            verify(ideaRepository).save(ideaCaptor.capture());
            Idea captured = ideaCaptor.getValue();
            assertEquals("Only Title Updated", captured.getTitle());
            assertEquals(originalDescription, captured.getDescription());
        }

        @Test
        @DisplayName("Deve atualizar apenas descrição quando fornecida")
        void updateIdea_withOnlyDescription_shouldUpdateOnlyDescription() {
            // Arrange
            ArgumentCaptor<Idea> ideaCaptor = ArgumentCaptor.forClass(Idea.class);
            when(ideaRepository.findById("test-id-123")).thenReturn(Optional.of(testIdea));
            when(ideaRepository.save(any(Idea.class))).thenReturn(testIdea);

            IdeaDTO partialDTO = new IdeaDTO();
            partialDTO.setTitle(null);
            partialDTO.setDescription("Only Description Updated");

            String originalTitle = testIdea.getTitle();

            // Act
            ideaService.updateIdea("test-id-123", partialDTO);

            // Assert
            verify(ideaRepository).save(ideaCaptor.capture());
            Idea captured = ideaCaptor.getValue();
            assertEquals(originalTitle, captured.getTitle());
            assertEquals("Only Description Updated", captured.getDescription());
        }

        @Test
        @DisplayName("Deve atualizar ambos os campos quando fornecidos")
        void updateIdea_withBothFields_shouldUpdateBoth() {
            // Arrange
            ArgumentCaptor<Idea> ideaCaptor = ArgumentCaptor.forClass(Idea.class);
            when(ideaRepository.findById("test-id-123")).thenReturn(Optional.of(testIdea));
            when(ideaRepository.save(any(Idea.class))).thenReturn(testIdea);

            IdeaDTO updateDTO = new IdeaDTO();
            updateDTO.setTitle("New Title");
            updateDTO.setDescription("New Description");

            // Act
            ideaService.updateIdea("test-id-123", updateDTO);

            // Assert
            verify(ideaRepository).save(ideaCaptor.capture());
            Idea captured = ideaCaptor.getValue();
            assertEquals("New Title", captured.getTitle());
            assertEquals("New Description", captured.getDescription());
        }

        @Test
        @DisplayName("Não deve atualizar nada quando campos são null")
        void updateIdea_withNullFields_shouldNotUpdate() {
            // Arrange
            ArgumentCaptor<Idea> ideaCaptor = ArgumentCaptor.forClass(Idea.class);
            when(ideaRepository.findById("test-id-123")).thenReturn(Optional.of(testIdea));
            when(ideaRepository.save(any(Idea.class))).thenReturn(testIdea);

            IdeaDTO emptyDTO = new IdeaDTO();
            emptyDTO.setTitle(null);
            emptyDTO.setDescription(null);

            String originalTitle = testIdea.getTitle();
            String originalDescription = testIdea.getDescription();

            // Act
            ideaService.updateIdea("test-id-123", emptyDTO);

            // Assert
            verify(ideaRepository).save(ideaCaptor.capture());
            Idea captured = ideaCaptor.getValue();
            assertEquals(originalTitle, captured.getTitle());
            assertEquals(originalDescription, captured.getDescription());
        }

        @Test
        @DisplayName("Deve lançar exceção quando ideia não existe")
        void updateIdea_whenNotExists_shouldThrowException() {
            // Arrange
            when(ideaRepository.findById("non-existent")).thenReturn(Optional.empty());

            IdeaDTO updateDTO = new IdeaDTO();
            updateDTO.setTitle("Updated");

            // Act & Assert
            assertThrows(RuntimeException.class,
                    () -> ideaService.updateIdea("non-existent", updateDTO));

            verify(ideaRepository, times(1)).findById("non-existent");
            verify(ideaRepository, never()).save(any(Idea.class));
        }

        @Test
        @DisplayName("Deve verificar se ideia existe antes de atualizar")
        void updateIdea_shouldCheckIfIdeaExistsBeforeUpdate() {
            // Arrange
            when(ideaRepository.findById("test-id-123")).thenReturn(Optional.of(testIdea));
            when(ideaRepository.save(any(Idea.class))).thenReturn(testIdea);

            IdeaDTO updateDTO = new IdeaDTO();
            updateDTO.setTitle("Updated Title");

            // Act
            ideaService.updateIdea("test-id-123", updateDTO);

            // Assert
            verify(ideaRepository, times(1)).findById("test-id-123");
        }
    }

    @Nested
    @DisplayName("deleteIdeaById Tests")
    class DeleteIdeaByIdTests {

        @Test
        @DisplayName("Deve deletar ideia por ID")
        void deleteIdeaById_shouldCallRepositoryDelete() {
            // Arrange
            doNothing().when(ideaRepository).deleteById("test-id-123");

            // Act
            ideaService.deleteIdeaById("test-id-123");

            // Assert
            verify(ideaRepository, times(1)).deleteById("test-id-123");
        }

        @Test
        @DisplayName("Deve chamar deleteById com ID correto")
        void deleteIdeaById_shouldPassCorrectId() {
            // Arrange
            String idToDelete = "specific-id-789";
            doNothing().when(ideaRepository).deleteById(idToDelete);

            // Act
            ideaService.deleteIdeaById(idToDelete);

            // Assert
            verify(ideaRepository, times(1)).deleteById(idToDelete);
        }

        @Test
        @DisplayName("Deve chamar repository apenas uma vez")
        void deleteIdeaById_shouldCallRepositoryOnce() {
            // Arrange
            doNothing().when(ideaRepository).deleteById("test-id-123");

            // Act
            ideaService.deleteIdeaById("test-id-123");

            // Assert
            verify(ideaRepository, times(1)).deleteById("test-id-123");
        }
    }
}