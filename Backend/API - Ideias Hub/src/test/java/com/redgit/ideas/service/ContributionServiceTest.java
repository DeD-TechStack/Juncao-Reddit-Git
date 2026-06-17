package com.redgit.ideas.service;

import com.redgit.ideas.controller.dto.ContributionCreateDTO;
import com.redgit.ideas.controller.dto.ContributionDTO;
import com.redgit.ideas.infrastructure.client.ReputationClient;
import com.redgit.ideas.infrastructure.entities.Contribution;
import com.redgit.ideas.infrastructure.entities.ContributionStatus;
import com.redgit.ideas.infrastructure.entities.Idea;
import com.redgit.ideas.infrastructure.repository.ContributionRepository;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContributionService Tests")
class ContributionServiceTest {

    @Mock
    private ContributionRepository contributionRepository;

    @Mock
    private IdeaRepository ideaRepository;

    @Mock
    private ReputationClient reputationClient;

    @InjectMocks
    private ContributionService contributionService;

    private Idea idea;
    private Contribution pendingContribution;

    @BeforeEach
    void setUp() {
        idea = new Idea();
        idea.setId("idea-1");
        idea.setTitle("Test Idea");
        idea.setDescription("Description");
        idea.setAuthorId("author@test.com");
        idea.setCreatedAt(LocalDateTime.now());

        pendingContribution = new Contribution();
        pendingContribution.setId("contrib-1");
        pendingContribution.setIdeaId("idea-1");
        pendingContribution.setContributorId("contributor@test.com");
        pendingContribution.setDiff("+ new line\n- old line");
        pendingContribution.setStatus(ContributionStatus.PENDING);
        pendingContribution.setCreatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("submit Tests")
    class SubmitTests {

        @Test
        @DisplayName("Deve criar contribuição PENDING para ideia existente")
        void submit_existingIdea_shouldReturnPendingContribution() {
            when(ideaRepository.existsById("idea-1")).thenReturn(true);
            when(contributionRepository.save(any(Contribution.class))).thenReturn(pendingContribution);

            ContributionCreateDTO dto = new ContributionCreateDTO("+ new line\n- old line");
            Contribution result = contributionService.submit("idea-1", "contributor@test.com", dto);

            assertNotNull(result);
            assertEquals(ContributionStatus.PENDING, result.getStatus());
            verify(contributionRepository, times(1)).save(any(Contribution.class));
        }

        @Test
        @DisplayName("Deve rejeitar submissão quando ideia não existe")
        void submit_ideaNotFound_shouldThrow404() {
            when(ideaRepository.existsById("non-existent")).thenReturn(false);

            ContributionCreateDTO dto = new ContributionCreateDTO("diff text");
            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> contributionService.submit("non-existent", "contributor@test.com", dto));

            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
            verify(contributionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve armazenar o usuário autenticado como contributorId")
        void submit_shouldStoreAuthenticatedUserAsContributorId() {
            when(ideaRepository.existsById("idea-1")).thenReturn(true);
            ArgumentCaptor<Contribution> captor = ArgumentCaptor.forClass(Contribution.class);
            when(contributionRepository.save(captor.capture())).thenReturn(pendingContribution);

            ContributionCreateDTO dto = new ContributionCreateDTO("diff text");
            contributionService.submit("idea-1", "contributor@test.com", dto);

            assertEquals("contributor@test.com", captor.getValue().getContributorId());
        }

        @Test
        @DisplayName("Deve salvar contribuição com status PENDING")
        void submit_shouldSaveWithPendingStatus() {
            when(ideaRepository.existsById("idea-1")).thenReturn(true);
            ArgumentCaptor<Contribution> captor = ArgumentCaptor.forClass(Contribution.class);
            when(contributionRepository.save(captor.capture())).thenReturn(pendingContribution);

            ContributionCreateDTO dto = new ContributionCreateDTO("+ added line");
            contributionService.submit("idea-1", "contributor@test.com", dto);

            assertEquals(ContributionStatus.PENDING, captor.getValue().getStatus());
            assertEquals("+ added line", captor.getValue().getDiff());
            assertEquals("idea-1", captor.getValue().getIdeaId());
            assertNotNull(captor.getValue().getCreatedAt());
        }
    }

    @Nested
    @DisplayName("listByIdea Tests")
    class ListByIdeaTests {

        @Test
        @DisplayName("Deve retornar contribuições de uma ideia")
        void listByIdea_shouldReturnContributions() {
            when(contributionRepository.findByIdeaId("idea-1"))
                    .thenReturn(List.of(pendingContribution));

            List<ContributionDTO> result = contributionService.listByIdea("idea-1");

            assertEquals(1, result.size());
            assertEquals("contrib-1", result.get(0).getId());
            assertEquals("contributor@test.com", result.get(0).getContributorId());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há contribuições")
        void listByIdea_noContributions_shouldReturnEmpty() {
            when(contributionRepository.findByIdeaId("idea-1")).thenReturn(List.of());

            List<ContributionDTO> result = contributionService.listByIdea("idea-1");

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("accept Tests")
    class AcceptTests {

        @Test
        @DisplayName("Deve alterar status para ACCEPTED quando o autor aceita")
        void accept_byIdeaAuthor_shouldSetStatusAccepted() {
            when(contributionRepository.findById("contrib-1")).thenReturn(Optional.of(pendingContribution));
            when(ideaRepository.findById("idea-1")).thenReturn(Optional.of(idea));
            ArgumentCaptor<Contribution> captor = ArgumentCaptor.forClass(Contribution.class);
            when(contributionRepository.save(captor.capture())).thenReturn(pendingContribution);

            contributionService.accept("contrib-1", "author@test.com");

            assertEquals(ContributionStatus.ACCEPTED, captor.getValue().getStatus());
        }

        @Test
        @DisplayName("Deve definir reviewedAt ao aceitar")
        void accept_shouldSetReviewedAt() {
            when(contributionRepository.findById("contrib-1")).thenReturn(Optional.of(pendingContribution));
            when(ideaRepository.findById("idea-1")).thenReturn(Optional.of(idea));
            ArgumentCaptor<Contribution> captor = ArgumentCaptor.forClass(Contribution.class);
            when(contributionRepository.save(captor.capture())).thenReturn(pendingContribution);

            contributionService.accept("contrib-1", "author@test.com");

            assertNotNull(captor.getValue().getReviewedAt());
        }

        @Test
        @DisplayName("Deve chamar notifyContributionAccepted com contributorId, não authorId")
        void accept_shouldCallReputationClientWithContributorId() {
            when(contributionRepository.findById("contrib-1")).thenReturn(Optional.of(pendingContribution));
            when(ideaRepository.findById("idea-1")).thenReturn(Optional.of(idea));
            when(contributionRepository.save(any())).thenReturn(pendingContribution);

            contributionService.accept("contrib-1", "author@test.com");

            verify(reputationClient, times(1))
                    .notifyContributionAccepted("contributor@test.com", "contrib-1");
            verify(reputationClient, never())
                    .notifyContributionAccepted("author@test.com", any());
        }

        @Test
        @DisplayName("Deve lançar 403 quando não é o autor da ideia")
        void accept_byNonAuthor_shouldThrow403() {
            when(contributionRepository.findById("contrib-1")).thenReturn(Optional.of(pendingContribution));
            when(ideaRepository.findById("idea-1")).thenReturn(Optional.of(idea));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> contributionService.accept("contrib-1", "other@test.com"));

            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
            verify(contributionRepository, never()).save(any());
            verify(reputationClient, never()).notifyContributionAccepted(any(), any());
        }

        @Test
        @DisplayName("Deve lançar 404 quando contribuição não existe")
        void accept_contributionNotFound_shouldThrow404() {
            when(contributionRepository.findById("missing")).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> contributionService.accept("missing", "author@test.com"));

            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }

        @Test
        @DisplayName("Não deve aceitar contribuição já aceita")
        void accept_alreadyAccepted_shouldThrowConflict() {
            pendingContribution.setStatus(ContributionStatus.ACCEPTED);
            pendingContribution.setReviewedAt(LocalDateTime.now().minusDays(1));

            when(contributionRepository.findById("contrib-1")).thenReturn(Optional.of(pendingContribution));
            when(ideaRepository.findById("idea-1")).thenReturn(Optional.of(idea));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> contributionService.accept("contrib-1", "author@test.com"));

            assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
            verify(contributionRepository, never()).save(any());
            verify(reputationClient, never()).notifyContributionAccepted(any(), any());
        }

        @Test
        @DisplayName("Não deve aceitar contribuição já rejeitada")
        void accept_alreadyRejected_shouldThrowConflict() {
            pendingContribution.setStatus(ContributionStatus.REJECTED);
            pendingContribution.setReviewedAt(LocalDateTime.now().minusDays(1));

            when(contributionRepository.findById("contrib-1")).thenReturn(Optional.of(pendingContribution));
            when(ideaRepository.findById("idea-1")).thenReturn(Optional.of(idea));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> contributionService.accept("contrib-1", "author@test.com"));

            assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
            verify(reputationClient, never()).notifyContributionAccepted(any(), any());
        }
    }

    @Nested
    @DisplayName("reject Tests")
    class RejectTests {

        @Test
        @DisplayName("Deve alterar status para REJECTED quando o autor rejeita")
        void reject_byIdeaAuthor_shouldSetStatusRejected() {
            when(contributionRepository.findById("contrib-1")).thenReturn(Optional.of(pendingContribution));
            when(ideaRepository.findById("idea-1")).thenReturn(Optional.of(idea));
            ArgumentCaptor<Contribution> captor = ArgumentCaptor.forClass(Contribution.class);
            when(contributionRepository.save(captor.capture())).thenReturn(pendingContribution);

            contributionService.reject("contrib-1", "author@test.com");

            assertEquals(ContributionStatus.REJECTED, captor.getValue().getStatus());
        }

        @Test
        @DisplayName("Deve definir reviewedAt ao rejeitar")
        void reject_shouldSetReviewedAt() {
            when(contributionRepository.findById("contrib-1")).thenReturn(Optional.of(pendingContribution));
            when(ideaRepository.findById("idea-1")).thenReturn(Optional.of(idea));
            ArgumentCaptor<Contribution> captor = ArgumentCaptor.forClass(Contribution.class);
            when(contributionRepository.save(captor.capture())).thenReturn(pendingContribution);

            contributionService.reject("contrib-1", "author@test.com");

            assertNotNull(captor.getValue().getReviewedAt());
        }

        @Test
        @DisplayName("Deve lançar 403 quando não é o autor da ideia")
        void reject_byNonAuthor_shouldThrow403() {
            when(contributionRepository.findById("contrib-1")).thenReturn(Optional.of(pendingContribution));
            when(ideaRepository.findById("idea-1")).thenReturn(Optional.of(idea));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> contributionService.reject("contrib-1", "other@test.com"));

            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
            verify(contributionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar 404 quando contribuição não existe")
        void reject_contributionNotFound_shouldThrow404() {
            when(contributionRepository.findById("missing")).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> contributionService.reject("missing", "author@test.com"));

            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }

        @Test
        @DisplayName("Não deve rejeitar contribuição já rejeitada")
        void reject_alreadyRejected_shouldThrowConflict() {
            pendingContribution.setStatus(ContributionStatus.REJECTED);
            pendingContribution.setReviewedAt(LocalDateTime.now().minusDays(1));

            when(contributionRepository.findById("contrib-1")).thenReturn(Optional.of(pendingContribution));
            when(ideaRepository.findById("idea-1")).thenReturn(Optional.of(idea));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> contributionService.reject("contrib-1", "author@test.com"));

            assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
            verify(contributionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Não deve rejeitar contribuição já aceita")
        void reject_alreadyAccepted_shouldThrowConflict() {
            pendingContribution.setStatus(ContributionStatus.ACCEPTED);
            pendingContribution.setReviewedAt(LocalDateTime.now().minusDays(1));

            when(contributionRepository.findById("contrib-1")).thenReturn(Optional.of(pendingContribution));
            when(ideaRepository.findById("idea-1")).thenReturn(Optional.of(idea));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> contributionService.reject("contrib-1", "author@test.com"));

            assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
            verify(contributionRepository, never()).save(any());
        }
    }
}
