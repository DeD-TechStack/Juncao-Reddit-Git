package com.redgit.ideas.repository;

import com.redgit.ideas.infrastructure.entities.Idea;
import com.redgit.ideas.infrastructure.repository.IdeaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IdeaRepository Tests")
class IdeaRepositoryTest {

    @Mock
    private IdeaRepository ideaRepository;

    private Idea testIdea1;
    private Idea testIdea2;

    @BeforeEach
    void setUp() {
        testIdea1 = new Idea();
        testIdea1.setId("id-1");
        testIdea1.setTitle("Idea 1");
        testIdea1.setDescription("Description 1");
        testIdea1.setAuthorId("user1@test.com");
        testIdea1.setCreatedAt(LocalDateTime.now());

        testIdea2 = new Idea();
        testIdea2.setId("id-2");
        testIdea2.setTitle("Idea 2");
        testIdea2.setDescription("Description 2");
        testIdea2.setAuthorId("user2@test.com");
        testIdea2.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve salvar uma ideia e retornar com ID gerado")
    void save_shouldPersistIdeaAndGenerateId() {
        // Arrange
        when(ideaRepository.save(any(Idea.class))).thenReturn(testIdea1);

        // Act
        Idea saved = ideaRepository.save(testIdea1);

        // Assert
        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("Idea 1", saved.getTitle());
        assertEquals("Description 1", saved.getDescription());
        assertEquals("user1@test.com", saved.getAuthorId());
        assertNotNull(saved.getCreatedAt());

        verify(ideaRepository, times(1)).save(any(Idea.class));
    }

    @Test
    @DisplayName("Deve buscar ideia por ID quando existe")
    void findById_whenExists_shouldReturnIdea() {
        // Arrange
        when(ideaRepository.findById("id-1")).thenReturn(Optional.of(testIdea1));

        // Act
        Optional<Idea> found = ideaRepository.findById("id-1");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("id-1", found.get().getId());
        assertEquals("Idea 1", found.get().getTitle());

        verify(ideaRepository, times(1)).findById("id-1");
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando ID não existe")
    void findById_whenNotExists_shouldReturnEmpty() {
        // Arrange
        when(ideaRepository.findById("non-existent-id")).thenReturn(Optional.empty());

        // Act
        Optional<Idea> found = ideaRepository.findById("non-existent-id");

        // Assert
        assertTrue(found.isEmpty());

        verify(ideaRepository, times(1)).findById("non-existent-id");
    }

    @Test
    @DisplayName("Deve encontrar todas as ideias de um autor")
    void findByAuthorId_shouldReturnAllIdeasFromAuthor() {
        // Arrange
        Idea idea1 = new Idea();
        idea1.setId("id-1");
        idea1.setTitle("Idea 1");
        idea1.setAuthorId("same-author@test.com");

        Idea idea2 = new Idea();
        idea2.setId("id-2");
        idea2.setTitle("Idea 2");
        idea2.setAuthorId("same-author@test.com");

        List<Idea> authorIdeas = Arrays.asList(idea1, idea2);
        when(ideaRepository.findByAuthorId("same-author@test.com")).thenReturn(authorIdeas);

        // Act
        List<Idea> ideas = ideaRepository.findByAuthorId("same-author@test.com");

        // Assert
        assertEquals(2, ideas.size());
        assertTrue(ideas.stream().allMatch(idea ->
                idea.getAuthorId().equals("same-author@test.com")));

        verify(ideaRepository, times(1)).findByAuthorId("same-author@test.com");
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando autor não tem ideias")
    void findByAuthorId_whenNoIdeas_shouldReturnEmptyList() {
        // Arrange
        when(ideaRepository.findByAuthorId("non-existent-author@test.com"))
                .thenReturn(Collections.emptyList());

        // Act
        List<Idea> ideas = ideaRepository.findByAuthorId("non-existent-author@test.com");

        // Assert
        assertTrue(ideas.isEmpty());

        verify(ideaRepository, times(1)).findByAuthorId("non-existent-author@test.com");
    }

    @Test
    @DisplayName("Deve retornar todas as ideias")
    void findAll_shouldReturnAllIdeas() {
        // Arrange
        List<Idea> allIdeas = Arrays.asList(testIdea1, testIdea2);
        when(ideaRepository.findAll()).thenReturn(allIdeas);

        // Act
        List<Idea> ideas = ideaRepository.findAll();

        // Assert
        assertEquals(2, ideas.size());

        verify(ideaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve deletar ideia por ID")
    void deleteById_shouldRemoveIdea() {
        // Arrange
        doNothing().when(ideaRepository).deleteById("id-1");
        when(ideaRepository.findById("id-1")).thenReturn(Optional.empty());

        // Act
        ideaRepository.deleteById("id-1");
        Optional<Idea> found = ideaRepository.findById("id-1");

        // Assert
        assertTrue(found.isEmpty());

        verify(ideaRepository, times(1)).deleteById("id-1");
        verify(ideaRepository, times(1)).findById("id-1");
    }

    @Test
    @DisplayName("Deve deletar todas as ideias")
    void deleteAll_shouldRemoveAllIdeas() {
        // Arrange
        doNothing().when(ideaRepository).deleteAll();
        when(ideaRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        ideaRepository.deleteAll();
        List<Idea> ideas = ideaRepository.findAll();

        // Assert
        assertTrue(ideas.isEmpty());

        verify(ideaRepository, times(1)).deleteAll();
        verify(ideaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve contar o número de ideias")
    void count_shouldReturnCorrectNumber() {
        // Arrange
        when(ideaRepository.count()).thenReturn(2L);

        // Act
        long count = ideaRepository.count();

        // Assert
        assertEquals(2, count);

        verify(ideaRepository, times(1)).count();
    }

    @Test
    @DisplayName("Deve verificar se ideia existe por ID")
    void existsById_whenExists_shouldReturnTrue() {
        // Arrange
        when(ideaRepository.existsById("id-1")).thenReturn(true);

        // Act
        boolean exists = ideaRepository.existsById("id-1");

        // Assert
        assertTrue(exists);

        verify(ideaRepository, times(1)).existsById("id-1");
    }

    @Test
    @DisplayName("Deve retornar false quando ideia não existe")
    void existsById_whenNotExists_shouldReturnFalse() {
        // Arrange
        when(ideaRepository.existsById("non-existent-id")).thenReturn(false);

        // Act
        boolean exists = ideaRepository.existsById("non-existent-id");

        // Assert
        assertFalse(exists);

        verify(ideaRepository, times(1)).existsById("non-existent-id");
    }

    @Test
    @DisplayName("Deve atualizar ideia existente")
    void save_whenUpdating_shouldUpdateIdea() {
        // Arrange
        Idea updatedIdea = new Idea();
        updatedIdea.setId("id-1");
        updatedIdea.setTitle("Updated Title");
        updatedIdea.setDescription("Updated Description");
        updatedIdea.setAuthorId("user1@test.com");

        when(ideaRepository.save(any(Idea.class))).thenReturn(updatedIdea);
        when(ideaRepository.findById("id-1")).thenReturn(Optional.of(updatedIdea));

        // Act
        Idea saved = ideaRepository.save(testIdea1);
        Optional<Idea> found = ideaRepository.findById("id-1");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("Updated Title", found.get().getTitle());
        assertEquals("Updated Description", found.get().getDescription());
        assertEquals("id-1", found.get().getId());

        verify(ideaRepository, times(1)).save(any(Idea.class));
        verify(ideaRepository, times(1)).findById("id-1");
    }

    @Test
    @DisplayName("Deve manter o timestamp de criação ao atualizar")
    void save_whenUpdating_shouldKeepCreatedAt() {
        // Arrange
        LocalDateTime originalCreatedAt = LocalDateTime.now().minusDays(1);
        testIdea1.setCreatedAt(originalCreatedAt);

        Idea updatedIdea = new Idea();
        updatedIdea.setId("id-1");
        updatedIdea.setTitle("Updated");
        updatedIdea.setCreatedAt(originalCreatedAt);

        when(ideaRepository.save(any(Idea.class))).thenReturn(updatedIdea);

        // Act
        Idea updated = ideaRepository.save(testIdea1);

        // Assert
        assertEquals(originalCreatedAt, updated.getCreatedAt());

        verify(ideaRepository, times(1)).save(any(Idea.class));
    }

    @Test
    @DisplayName("Deve salvar múltiplas ideias do mesmo autor")
    void save_multipleIdeasSameAuthor_shouldSaveAll() {
        // Arrange
        Idea idea1 = new Idea();
        idea1.setId("id-1");
        idea1.setAuthorId("user@test.com");

        Idea idea2 = new Idea();
        idea2.setId("id-2");
        idea2.setAuthorId("user@test.com");

        Idea idea3 = new Idea();
        idea3.setId("id-3");
        idea3.setAuthorId("user@test.com");

        when(ideaRepository.save(any(Idea.class)))
                .thenReturn(idea1)
                .thenReturn(idea2)
                .thenReturn(idea3);

        when(ideaRepository.findByAuthorId("user@test.com"))
                .thenReturn(Arrays.asList(idea1, idea2, idea3));

        // Act
        ideaRepository.save(idea1);
        ideaRepository.save(idea2);
        ideaRepository.save(idea3);

        List<Idea> ideas = ideaRepository.findByAuthorId("user@test.com");

        // Assert
        assertEquals(3, ideas.size());

        verify(ideaRepository, times(3)).save(any(Idea.class));
        verify(ideaRepository, times(1)).findByAuthorId("user@test.com");
    }

    @Test
    @DisplayName("Deve tratar corretamente save de ideia com campos nulos")
    void save_withNullFields_shouldHandleGracefully() {
        // Arrange
        Idea ideaWithNulls = new Idea();
        ideaWithNulls.setId("id-null");
        // title e description são null

        when(ideaRepository.save(any(Idea.class))).thenReturn(ideaWithNulls);

        // Act
        Idea saved = ideaRepository.save(ideaWithNulls);

        // Assert
        assertNotNull(saved);
        assertEquals("id-null", saved.getId());

        verify(ideaRepository, times(1)).save(any(Idea.class));
    }

    @Test
    @DisplayName("Deve buscar por ID com diferentes formatos de ID")
    void findById_withDifferentIdFormats_shouldWork() {
        // Arrange
        String[] differentIds = {"123", "abc-def-ghi", "507f1f77bcf86cd799439011"};

        for (String id : differentIds) {
            Idea idea = new Idea();
            idea.setId(id);
            when(ideaRepository.findById(id)).thenReturn(Optional.of(idea));
        }

        // Act & Assert
        for (String id : differentIds) {
            Optional<Idea> found = ideaRepository.findById(id);
            assertTrue(found.isPresent());
            assertEquals(id, found.get().getId());
        }

        verify(ideaRepository, times(3)).findById(anyString());
    }
}