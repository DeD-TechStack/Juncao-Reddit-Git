package com.daniel.registry.reputation.dto;

public record ReputationResponseDTO(
        String userEmail,
        long xp,
        int level,
        String title
) {}
