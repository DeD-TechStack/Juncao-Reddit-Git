package com.redgit.ideas.controller;

import com.redgit.ideas.controller.dto.IdeaCreateDTO;
import com.redgit.ideas.controller.dto.IdeaDTO;
import com.redgit.ideas.infrastructure.entities.Idea;
import com.redgit.ideas.service.IdeaService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/ideas")
@RequiredArgsConstructor
public class IdeaController {

    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    private final IdeaService ideaService;

    @PostMapping
    public ResponseEntity<Idea> createIdea(
            @Validated @RequestBody IdeaCreateDTO ideaCreateDTO,
            @AuthenticationPrincipal String userEmail) {

        Idea created = ideaService.createIdea(ideaCreateDTO, userEmail);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<Idea>> getAllIdeas(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ideaService.getAllIdeas(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Idea> getIdeaById(@PathVariable String id) {
        return ResponseEntity.ok(ideaService.findById(id));
    }

    @GetMapping("/my-ideas")
    public ResponseEntity<Page<Idea>> getMyIdeas(
            @AuthenticationPrincipal String userEmail,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ideaService.getIdeasByAuthor(userEmail, pageable));
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<Page<Idea>> getIdeasByAuthor(
            @PathVariable String authorId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ideaService.getIdeasByAuthor(authorId, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Idea> replaceIdea(
            @PathVariable String id,
            @Validated @RequestBody IdeaDTO ideaDTO,
            @AuthenticationPrincipal String userEmail) {

        Idea existing = ideaService.findById(id);
        if (!existing.getAuthorId().equals(userEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Idea updated = ideaService.replaceIdea(id, ideaDTO);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Idea> updateIdea(
            @PathVariable String id,
            @RequestBody IdeaDTO ideaDTO,
            @AuthenticationPrincipal String userEmail) {

        Idea existing = ideaService.findById(id);
        if (!existing.getAuthorId().equals(userEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Idea updated = ideaService.updateIdea(id, ideaDTO);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Idea> likeIdea(
            @PathVariable String id,
            @AuthenticationPrincipal String userEmail) {
        return ResponseEntity.ok(ideaService.likeIdea(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIdea(
            @PathVariable String id,
            @AuthenticationPrincipal String userEmail) {

        Idea existing = ideaService.findById(id);
        if (!existing.getAuthorId().equals(userEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ideaService.deleteIdeaById(id);
        audit.info("[AUDIT] DELETE_IDEA | executadoPor={} | ideaId={} | ts={}", userEmail, id, Instant.now());
        return ResponseEntity.noContent().build();
    }
}
