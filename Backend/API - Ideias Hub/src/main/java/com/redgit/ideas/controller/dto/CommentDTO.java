package com.redgit.ideas.controller.dto;

import com.redgit.ideas.infrastructure.entities.Comment;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class CommentDTO {

    private String id;
    private String ideaId;
    private String authorId;
    private String parentId;
    private String content;
    private LocalDateTime createdAt;
    private boolean deleted;
    private List<CommentDTO> replies;

    public CommentDTO() {
        this.replies = new ArrayList<>();
    }

    public static CommentDTO from(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setIdeaId(comment.getIdeaId());
        dto.setAuthorId(comment.getAuthorId());
        dto.setParentId(comment.getParentId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setDeleted(comment.getDeletedAt() != null);
        return dto;
    }
}
