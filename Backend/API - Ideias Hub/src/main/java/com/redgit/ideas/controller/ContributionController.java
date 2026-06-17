package com.redgit.ideas.controller;

import com.redgit.ideas.controller.dto.ContributionCreateDTO;
import com.redgit.ideas.controller.dto.ContributionDTO;
import com.redgit.ideas.infrastructure.entities.Contribution;
import com.redgit.ideas.service.ContributionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ContributionController {

    private final ContributionService contributionService;

    @PostMapping("/api/ideas/{ideaId}/contributions")
    public ResponseEntity<ContributionDTO> submitContribution(
            @PathVariable String ideaId,
            @Validated @RequestBody ContributionCreateDTO dto,
            @AuthenticationPrincipal String userEmail) {
        Contribution contribution = contributionService.submit(ideaId, userEmail, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ContributionDTO.from(contribution));
    }

    @GetMapping("/api/ideas/{ideaId}/contributions")
    public ResponseEntity<List<ContributionDTO>> listContributions(@PathVariable String ideaId) {
        return ResponseEntity.ok(contributionService.listByIdea(ideaId));
    }

    @PatchMapping("/api/contributions/{contributionId}/accept")
    public ResponseEntity<Void> acceptContribution(
            @PathVariable String contributionId,
            @AuthenticationPrincipal String userEmail) {
        contributionService.accept(contributionId, userEmail);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/contributions/{contributionId}/reject")
    public ResponseEntity<Void> rejectContribution(
            @PathVariable String contributionId,
            @AuthenticationPrincipal String userEmail) {
        contributionService.reject(contributionId, userEmail);
        return ResponseEntity.noContent().build();
    }
}
