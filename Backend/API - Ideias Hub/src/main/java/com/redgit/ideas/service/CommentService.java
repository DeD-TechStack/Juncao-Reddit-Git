package com.redgit.ideas.service;

import com.redgit.ideas.controller.dto.CommentCreateDTO;
import com.redgit.ideas.controller.dto.CommentDTO;
import com.redgit.ideas.infrastructure.entities.Comment;
import com.redgit.ideas.infrastructure.repository.CommentRepository;
import com.redgit.ideas.infrastructure.repository.IdeaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final IdeaRepository ideaRepository;

    public Comment createComment(String ideaId, String authorId, CommentCreateDTO dto) {
        if (!ideaRepository.existsById(ideaId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ideia não encontrada: " + ideaId);
        }

        String parentId = dto.parentId();
        if (parentId != null && !parentId.isBlank()) {
            Comment parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Comentário pai não encontrado: " + parentId));
            if (!parent.getIdeaId().equals(ideaId)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Comentário pai pertence a outra ideia");
            }
        } else {
            parentId = null;
        }

        Comment comment = new Comment();
        comment.setIdeaId(ideaId);
        comment.setAuthorId(authorId);
        comment.setParentId(parentId);
        comment.setContent(dto.content());
        comment.setCreatedAt(LocalDateTime.now());

        return commentRepository.save(comment);
    }

    public List<CommentDTO> listComments(String ideaId) {
        List<Comment> all = commentRepository.findByIdeaId(ideaId);

        Map<String, CommentDTO> byId = new LinkedHashMap<>();
        for (Comment c : all) {
            byId.put(c.getId(), CommentDTO.from(c));
        }

        List<CommentDTO> roots = new ArrayList<>();
        for (Comment c : all) {
            CommentDTO dto = byId.get(c.getId());
            if (c.getParentId() == null) {
                roots.add(dto);
            } else {
                CommentDTO parentDto = byId.get(c.getParentId());
                if (parentDto != null) {
                    parentDto.getReplies().add(dto);
                } else {
                    roots.add(dto);
                }
            }
        }

        return roots;
    }

    public void softDelete(String commentId, String requestingUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Comentário não encontrado: " + commentId));

        if (!comment.getAuthorId().equals(requestingUserId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Apenas o autor pode deletar este comentário");
        }

        if (comment.getDeletedAt() == null) {
            comment.setContent("removido");
            comment.setDeletedAt(LocalDateTime.now());
            commentRepository.save(comment);
        }
    }
}
