package com.redgit.ideas.controller.dto;

import com.redgit.ideas.validation.NoHtml;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentCreateDTO(
        @NotBlank(message = "Conteúdo obrigatório")
        @Size(max = 2000, message = "Comentário deve ter no máximo 2000 caracteres")
        @NoHtml
        String content,

        String parentId
) {}
