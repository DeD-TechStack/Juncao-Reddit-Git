package com.redgit.ideas.controller;

import com.redgit.ideas.controller.dto.ContributionCreateDTO;
import com.redgit.ideas.controller.dto.ContributionDTO;
import com.redgit.ideas.infrastructure.entities.Contribution;
import com.redgit.ideas.infrastructure.entities.ContributionStatus;
import com.redgit.ideas.service.ContributionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
@DisplayName("ContributionController Tests")
class ContributionControllerTest {

    @Mock
    private ContributionService contributionService;

    @InjectMocks
    private ContributionController contributionController;

    private Contribution testContribution;
    private ContributionDTO testContributionDTO;

    @BeforeEach
    void setUp() {
        testContribution = new Contribution();
        testContribution.setId("contrib-1");
        testContribution.setIdeaId("idea-1");
        testContribution.setContributorId("contributor@test.com");
        testContribution.setDiff("+ new line\n- old line");
        testContribution.setStatus(ContributionStatus.PENDING);
        testContribution.setCreatedAt(LocalDateTime.now());

        testContributionDTO = ContributionDTO.from(testContribution);
    }

    @Nested
    @DisplayName("submitContribution Tests")
    class SubmitContributionTests {

        @Test
        @DisplayName("Deve retornar 201 ao submeter contribuição")
        void submitContribution_shouldReturn201() {
            when(contributionService.submit(eq("idea-1"), eq("contributor@test.com"), any(ContributionCreateDTO.class)))
                    .thenReturn(testContribution);

            ContributionCreateDTO dto = new ContributionCreateDTO("+ new line\n- old line");
            ResponseEntity<ContributionDTO> response =
                    contributionController.submitContribution("idea-1", dto, "contributor@test.com");

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("contrib-1", response.getBody().getId());
            assertEquals(ContributionStatus.PENDING, response.getBody().getStatus());
        }

        @Test
        @DisplayName("Deve propagar 404 quando ideia não existe")
        void submitContribution_ideaNotFound_shouldPropagate404() {
            when(contributionService.submit(eq("missing"), eq("contributor@test.com"), any()))
                    .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Ideia não encontrada"));

            ContributionCreateDTO dto = new ContributionCreateDTO("diff");
            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> contributionController.submitContribution("missing", dto, "contributor@test.com"));

            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }
    }

    @Nested
    @DisplayName("listContributions Tests")
    class ListContributionsTests {

        @Test
        @DisplayName("Deve retornar 200 com lista de contribuições")
        void listContributions_shouldReturn200() {
            when(contributionService.listByIdea("idea-1")).thenReturn(List.of(testContributionDTO));

            ResponseEntity<List<ContributionDTO>> response =
                    contributionController.listContributions("idea-1");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
            assertEquals("contrib-1", response.getBody().get(0).getId());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há contribuições")
        void listContributions_noContributions_shouldReturnEmpty() {
            when(contributionService.listByIdea("idea-1")).thenReturn(List.of());

            ResponseEntity<List<ContributionDTO>> response =
                    contributionController.listContributions("idea-1");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isEmpty());
        }
    }

    @Nested
    @DisplayName("acceptContribution Tests")
    class AcceptContributionTests {

        @Test
        @DisplayName("Deve retornar 204 ao aceitar contribuição")
        void acceptContribution_shouldReturn204() {
            doNothing().when(contributionService).accept("contrib-1", "author@test.com");

            ResponseEntity<Void> response =
                    contributionController.acceptContribution("contrib-1", "author@test.com");

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            verify(contributionService, times(1)).accept("contrib-1", "author@test.com");
        }

        @Test
        @DisplayName("Deve propagar 403 quando não é o autor da ideia")
        void acceptContribution_notAuthor_shouldPropagate403() {
            doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas o autor da ideia pode aceitar contribuições"))
                    .when(contributionService).accept("contrib-1", "other@test.com");

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> contributionController.acceptContribution("contrib-1", "other@test.com"));

            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        }

        @Test
        @DisplayName("Deve propagar 404 quando contribuição não existe")
        void acceptContribution_notFound_shouldPropagate404() {
            doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Contribuição não encontrada"))
                    .when(contributionService).accept("missing", "author@test.com");

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> contributionController.acceptContribution("missing", "author@test.com"));

            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }

        @Test
        @DisplayName("Deve propagar 409 quando contribuição já foi processada")
        void acceptContribution_alreadyProcessed_shouldPropagate409() {
            doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Contribuição não está pendente"))
                    .when(contributionService).accept("contrib-1", "author@test.com");

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> contributionController.acceptContribution("contrib-1", "author@test.com"));

            assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        }
    }

    @Nested
    @DisplayName("rejectContribution Tests")
    class RejectContributionTests {

        @Test
        @DisplayName("Deve retornar 204 ao rejeitar contribuição")
        void rejectContribution_shouldReturn204() {
            doNothing().when(contributionService).reject("contrib-1", "author@test.com");

            ResponseEntity<Void> response =
                    contributionController.rejectContribution("contrib-1", "author@test.com");

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            verify(contributionService, times(1)).reject("contrib-1", "author@test.com");
        }

        @Test
        @DisplayName("Deve propagar 403 quando não é o autor da ideia")
        void rejectContribution_notAuthor_shouldPropagate403() {
            doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas o autor da ideia pode rejeitar contribuições"))
                    .when(contributionService).reject("contrib-1", "other@test.com");

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> contributionController.rejectContribution("contrib-1", "other@test.com"));

            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        }
    }
}
