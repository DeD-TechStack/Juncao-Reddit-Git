package com.redgit.ideas.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContributionCreateDTO(
        @NotBlank(message = "Diff obrigatório")
        @Size(max = 10000, message = "Diff deve ter no máximo 10000 caracteres")
        String diff
) {}
