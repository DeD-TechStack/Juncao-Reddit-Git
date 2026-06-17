package com.redgit.ideas.controller;

import com.redgit.ideas.controller.dto.CommentCreateDTO;
import com.redgit.ideas.controller.dto.CommentDTO;
import com.redgit.ideas.infrastructure.entities.Comment;
import com.redgit.ideas.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/api/ideas/{ideaId}/comments")
    public ResponseEntity<CommentDTO> createComment(
            @PathVariable String ideaId,
            @Validated @RequestBody CommentCreateDTO dto,
            @AuthenticationPrincipal String userEmail) {
        Comment comment = commentService.createComment(ideaId, userEmail, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommentDTO.from(comment));
    }

    @GetMapping("/api/ideas/{ideaId}/comments")
    public ResponseEntity<List<CommentDTO>> listComments(@PathVariable String ideaId) {
        return ResponseEntity.ok(commentService.listComments(ideaId));
    }

    @DeleteMapping("/api/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable String commentId,
            @AuthenticationPrincipal String userEmail) {
        commentService.softDelete(commentId, userEmail);
        return ResponseEntity.noContent().build();
    }
}
