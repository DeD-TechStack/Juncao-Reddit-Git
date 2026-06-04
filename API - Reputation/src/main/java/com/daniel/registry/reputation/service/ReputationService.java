package com.daniel.registry.reputation.service;

import com.daniel.registry.reputation.dto.ReputationEventResponseDTO;
import com.daniel.registry.reputation.dto.ReputationResponseDTO;
import com.daniel.registry.reputation.infrastructure.entity.ReputationEventType;
import com.daniel.registry.reputation.infrastructure.entity.UserReputation;
import com.daniel.registry.reputation.infrastructure.repository.UserReputationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReputationService {

    private final UserReputationRepository repo;

    public ReputationService(UserReputationRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public ReputationEventResponseDTO applyEvent(String userEmail, ReputationEventType type) {
        UserReputation rep = repo.findByUserEmail(userEmail)
                .orElseGet(() -> repo.save(new UserReputation(userEmail)));

        long gained = gainedXp(type);
        long newXp = rep.getXp() + gained;

        LevelInfo info = computeLevel(newXp);

        rep.apply(newXp, info.level(), info.title());

        return new ReputationEventResponseDTO(
                type.name(),
                gained,
                rep.getXp(),
                rep.getLevel(),
                rep.getTitle()
        );
    }

    @Transactional(readOnly = true)
    public ReputationResponseDTO me(String userEmail) {
        UserReputation rep = repo.findByUserEmail(userEmail)
                .orElse(new UserReputation(userEmail));

        return new ReputationResponseDTO(rep.getUserEmail(), rep.getXp(), rep.getLevel(), rep.getTitle());
    }

    private long gainedXp(ReputationEventType type) {
        return switch (type) {
            case LIKE_GAINED -> 10;
            case CONTRIBUTION_ACCEPTED -> 50;
            case IDEA_TRENDING -> 100;
        };
    }

    private LevelInfo computeLevel(long xp) {
        if (xp < 100) return new LevelInfo(1, "Inovador Iniciante");
        if (xp < 300) return new LevelInfo(2, "Criador Promissor");
        if (xp < 600) return new LevelInfo(3, "Arquiteto de Ideias");
        if (xp < 1000) return new LevelInfo(4, "Visionário");
        if (xp < 1600) return new LevelInfo(5, "Mestre da Comunidade");
        return new LevelInfo(6, "Lenda do Hub");
    }

    private record LevelInfo(int level, String title) {}
}
