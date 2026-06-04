package com.daniel.registry.reputation.service;

import com.daniel.registry.reputation.dto.ReputationEventResponseDTO;
import com.daniel.registry.reputation.infrastructure.entity.ReputationEventType;
import com.daniel.registry.reputation.infrastructure.entity.UserReputation;
import com.daniel.registry.reputation.infrastructure.repository.UserReputationRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReputationServiceTest {

    @Test
    void applyEvent_criaUsuarioSeNaoExistir_eSomaXp() {
        UserReputationRepository repo = mock(UserReputationRepository.class);
        ReputationService service = new ReputationService(repo);

        when(repo.findByUserEmail("a@a.com")).thenReturn(Optional.empty());
        when(repo.save(any(UserReputation.class))).thenAnswer(inv -> inv.getArgument(0));

        ReputationEventResponseDTO res = service.applyEvent("a@a.com", ReputationEventType.IDEA_TRENDING);

        assertEquals("IDEA_TRENDING", res.eventType());
        assertEquals(100, res.gainedXp());
        assertEquals(100, res.totalXp());
        assertEquals(2, res.level());
    }

    @Test
    void applyEvent_atualizaLevelCorreto() {
        UserReputationRepository repo = mock(UserReputationRepository.class);
        ReputationService service = new ReputationService(repo);

        UserReputation existing = new UserReputation("a@a.com");
        existing.apply(590, 3, "Arquiteto de Ideias");

        when(repo.findByUserEmail("a@a.com")).thenReturn(Optional.of(existing));

        ReputationEventResponseDTO res = service.applyEvent("a@a.com", ReputationEventType.CONTRIBUTION_ACCEPTED);
        assertEquals(640, res.totalXp());
        assertEquals(4, res.level());
        assertEquals("Visionário", res.title());
    }
}
