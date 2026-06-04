package com.daniel.registry.reputation.dto;

public record ReputationEventResponseDTO(
        String eventType,
        long gainedXp,
        long totalXp,
        int level,
        String title
) {}
