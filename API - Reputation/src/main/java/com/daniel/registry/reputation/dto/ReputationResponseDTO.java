package com.daniel.registry.reputation.dto;

public record ReputationResponseDTO(
        String userId,
        long xp,
        int level,
        String title
) {}
