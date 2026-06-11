package com.daniel.registry.reputation.dto;

import com.daniel.registry.reputation.infrastructure.entity.ReputationEventType;
import jakarta.validation.constraints.NotNull;

public record ReputationEventRequestDTO(
        @NotNull(message = "Tipo de evento obrigatório")
        ReputationEventType type,

        String referenceId
) {}
