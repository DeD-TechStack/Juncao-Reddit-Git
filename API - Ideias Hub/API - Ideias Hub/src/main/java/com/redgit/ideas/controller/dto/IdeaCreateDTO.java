package com.redgit.ideas.controller.dto;

import com.redgit.ideas.validation.NoHtml;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IdeaCreateDTO(
        @NotBlank(message = "Título obrigatório")
        @Size(max = 200, message = "Título deve ter no máximo 200 caracteres")
        @NoHtml
        String title,

        @NotBlank(message = "Descrição obrigatória")
        @Size(max = 5000, message = "Descrição deve ter no máximo 5000 caracteres")
        @NoHtml
        String description
) {}
