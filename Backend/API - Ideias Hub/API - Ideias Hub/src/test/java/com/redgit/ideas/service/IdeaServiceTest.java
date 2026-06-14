package com.redgit.ideas.service;

import com.redgit.ideas.controller.dto.IdeaCreateDTO;
import com.redgit.ideas.controller.dto.IdeaDTO;
import com.redgit.ideas.infrastructure.client.ReputationClient;
import com.redgit.ideas.infrastructure.client.TrendingClient;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IdeaService Tests")
class IdeaServiceTest {

    @Mock
    private IdeaRepository ideaRepository;

    @Mock
    private TrendingClient trendingClient;

    @Mock
    private ReputationClient reputationClient;

    @InjectMocks
    private IdeaService ideaService;

    private IdeaCreateDTO testCreateDTO;
    private IdeaDTO testDTO;
    private Idea testIdea;

    @BeforeEach
    void setUp() {
        testCreateDTO = new IdeaCreateDTO("Test Idea", "Test Description");

        testDTO = new IdeaDTO();
        testDTO.setTitle("Test Idea");
        testDTO.setDescription("Test Description");

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
            Idea result = ideaService.createIdea(testCreateDTO, "user@test.com");

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
            ideaService.createIdea(testCreateDTO, "user@test.com");

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
            ideaService.createIdea(testCreateDTO, "user@test.com");

            // Assert
            verify(ideaRepository).save(ideaCaptor.capture());
            Idea captured = ideaCaptor.getValue();
            assertEquals(testCreateDTO.title(), captured.getTitle());
            assertEquals(testCreateDTO.description(), captured.getDescription());
            assertEquals("user@test.com", captured.getAuthorId());
        }

        @Test
        @DisplayName("Deve criar nova instância de Idea")
        void createIdea_shouldCreateNewIdeaInstance() {
            // Arrange
            ArgumentCaptor<Idea> ideaCaptor = ArgumentCaptor.forClass(Idea.class);
            when(ideaRepository.save(any(Idea.class))).thenReturn(testIdea);

            // Act
            ideaService.createIdea(testCreateDTO, "user@test.com");

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
            when(ideaRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(ideas));

            // Act
            Page<Idea> result = ideaService.getAllIdeas(Pageable.unpaged());

            // Assert
            assertNotNull(result);
            assertEquals(2, result.getTotalElements());
            assertEquals("Test Idea", result.getContent().get(0).getTitle());
            assertEquals("Another Idea", result.getContent().get(1).getTitle());
            verify(ideaRepository, times(1)).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("Deve retornar página vazia quando não há ideias")
        void getAllIdeas_whenEmpty_shouldReturnEmptyPage() {
            // Arrange
            when(ideaRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

            // Act
            Page<Idea> result = ideaService.getAllIdeas(Pageable.unpaged());

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(ideaRepository, times(1)).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("Deve chamar repository.findAll apenas uma vez")
        void getAllIdeas_shouldCallRepositoryOnce() {
            // Arrange
            when(ideaRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(Arrays.asList(testIdea)));

            // Act
            ideaService.getAllIdeas(Pageable.unpaged());

            // Assert
            verify(ideaRepository, times(1)).findAll(any(Pageable.class));
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
            assertThrows(RuntimeException.class,
                    () -> ideaService.findById("non-existent"));

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
            when(ideaRepository.findByAuthorId(eq("user@test.com"), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(ideas));

            // Act
            Page<Idea> result = ideaService.getIdeasByAuthor("user@test.com", Pageable.unpaged());

            // Assert
            assertNotNull(result);
            assertEquals(2, result.getTotalElements());
            assertTrue(result.getContent().stream().allMatch(
                    idea -> "user@test.com".equals(idea.getAuthorId())));
            verify(ideaRepository, times(1)).findByAuthorId(eq("user@test.com"), any(Pageable.class));
        }

        @Test
        @DisplayName("Deve retornar página vazia quando autor não tem ideias")
        void getIdeasByAuthor_whenNoIdeas_shouldReturnEmptyPage() {
            // Arrange
            when(ideaRepository.findByAuthorId(eq("another@test.com"), any(Pageable.class)))
                    .thenReturn(Page.empty());

            // Act
            Page<Idea> result = ideaService.getIdeasByAuthor("another@test.com", Pageable.unpaged());

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(ideaRepository, times(1)).findByAuthorId(eq("another@test.com"), any(Pageable.class));
        }

        @Test
        @DisplayName("Deve chamar repository com authorId correto")
        void getIdeasByAuthor_shouldCallRepositoryWithCorrectAuthorId() {
            // Arrange
            when(ideaRepository.findByAuthorId(anyString(), any(Pageable.class))).thenReturn(Page.empty());

            // Act
            ideaService.getIdeasByAuthor("specific@author.com", Pageable.unpaged());

            // Assert
            verify(ideaRepository).findByAuthorId(eq("specific@author.com"), any(Pageable.class));
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
    @DisplayName("likeIdea Tests")
    class LikeIdeaTests {

        @Test
        @DisplayName("Deve incrementar likesCount ao curtir")
        void likeIdea_shouldIncrementLikesCount() {
            // Arrange
            testIdea.setLikesCount(0L);
            Idea liked = new Idea();
            liked.setId("test-id-123");
            liked.setLikesCount(1L);
            when(ideaRepository.findById("test-id-123")).thenReturn(Optional.of(testIdea));
            when(ideaRepository.save(any(Idea.class))).thenReturn(liked);

            // Act
            Idea result = ideaService.likeIdea("test-id-123");

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getLikesCount());
            verify(ideaRepository, times(1)).findById("test-id-123");
            verify(ideaRepository, times(1)).save(any(Idea.class));
        }

        @Test
        @DisplayName("Deve salvar a ideia com contador incrementado")
        void likeIdea_shouldSaveIdeaWithIncrementedCounter() {
            // Arrange
            ArgumentCaptor<Idea> ideaCaptor = ArgumentCaptor.forClass(Idea.class);
            testIdea.setLikesCount(5L);
            when(ideaRepository.findById("test-id-123")).thenReturn(Optional.of(testIdea));
            when(ideaRepository.save(any(Idea.class))).thenReturn(testIdea);

            // Act
            ideaService.likeIdea("test-id-123");

            // Assert
            verify(ideaRepository).save(ideaCaptor.capture());
            assertEquals(6L, ideaCaptor.getValue().getLikesCount());
        }

        @Test
        @DisplayName("Deve lançar exceção quando ideia não existe")
        void likeIdea_whenNotExists_shouldThrowException() {
            // Arrange
            when(ideaRepository.findById("non-existent")).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(RuntimeException.class,
                    () -> ideaService.likeIdea("non-existent"));

            verify(ideaRepository, times(1)).findById("non-existent");
            verify(ideaRepository, never()).save(any(Idea.class));
        }

        @Test
        @DisplayName("Deve permitir múltiplos likes (comportamento não-idempotente)")
        void likeIdea_calledMultipleTimes_shouldAccumulateLikes() {
            // Arrange — simula contador crescendo a cada save
            testIdea.setLikesCount(0L);
            when(ideaRepository.findById("test-id-123")).thenReturn(Optional.of(testIdea));
            when(ideaRepository.save(any(Idea.class))).thenAnswer(invocation -> {
                Idea saved = invocation.getArgument(0);
                testIdea.setLikesCount(saved.getLikesCount());
                return saved;
            });

            // Act
            ideaService.likeIdea("test-id-123");
            ideaService.likeIdea("test-id-123");
            ideaService.likeIdea("test-id-123");

            // Assert
            assertEquals(3L, testIdea.getLikesCount());
            verify(ideaRepository, times(3)).save(any(Idea.class));
        }

        @Test
        @DisplayName("Deve notificar o servico Trending ao curtir")
        void likeIdea_shouldNotifyTrending() {
            // Arrange
            testIdea.setLikesCount(0L);
            when(ideaRepository.findById("test-id-123")).thenReturn(Optional.of(testIdea));
            when(ideaRepository.save(any(Idea.class))).thenReturn(testIdea);

            // Act
            ideaService.likeIdea("test-id-123");

            // Assert
            verify(trendingClient, times(1)).notifyLike("test-id-123");
        }

        @Test
        @DisplayName("Deve concluir o like normalmente mesmo se a notificacao ao Trending falhar")
        void likeIdea_whenTrendingNotificationFails_shouldStillReturnLikedIdea() {
            // Arrange
            testIdea.setLikesCount(0L);
            Idea liked = new Idea();
            liked.setId("test-id-123");
            liked.setLikesCount(1L);
            when(ideaRepository.findById("test-id-123")).thenReturn(Optional.of(testIdea));
            when(ideaRepository.save(any(Idea.class))).thenReturn(liked);
            doThrow(new RuntimeException("Trending indisponivel"))
                    .when(trendingClient).notifyLike("test-id-123");

            // Act
            Idea result = ideaService.likeIdea("test-id-123");

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getLikesCount());
            verify(ideaRepository, times(1)).save(any(Idea.class));
            verify(trendingClient, times(1)).notifyLike("test-id-123");
        }

        @Test
        @DisplayName("Deve notificar o servico Reputation com o autor da ideia ao curtir")
        void likeIdea_shouldNotifyReputation() {
            // Arrange
            testIdea.setLikesCount(0L);
            testIdea.setAuthorId("author@test.com");
            when(ideaRepository.findById("test-id-123")).thenReturn(Optional.of(testIdea));
            when(ideaRepository.save(any(Idea.class))).thenReturn(testIdea);

            // Act
            ideaService.likeIdea("test-id-123");

            // Assert
            verify(reputationClient, times(1)).notifyLikeGained("author@test.com", "test-id-123");
        }

        @Test
        @DisplayName("Deve concluir o like normalmente mesmo se a notificacao ao Reputation falhar")
        void likeIdea_whenReputationNotificationFails_shouldStillReturnLikedIdea() {
            // Arrange
            testIdea.setLikesCount(0L);
            testIdea.setAuthorId("author@test.com");
            Idea liked = new Idea();
            liked.setId("test-id-123");
            liked.setAuthorId("author@test.com");
            liked.setLikesCount(1L);
            when(ideaRepository.findById("test-id-123")).thenReturn(Optional.of(testIdea));
            when(ideaRepository.save(any(Idea.class))).thenReturn(liked);
            doThrow(new RuntimeException("Reputation indisponivel"))
                    .when(reputationClient).notifyLikeGained("author@test.com", "test-id-123");

            // Act
            Idea result = ideaService.likeIdea("test-id-123");

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getLikesCount());
            verify(ideaRepository, times(1)).save(any(Idea.class));
            verify(reputationClient, times(1)).notifyLikeGained("author@test.com", "test-id-123");
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
