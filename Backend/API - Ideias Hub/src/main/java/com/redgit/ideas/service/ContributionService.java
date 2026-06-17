package com.redgit.ideas.service;

import com.redgit.ideas.controller.dto.ContributionCreateDTO;
import com.redgit.ideas.controller.dto.ContributionDTO;
import com.redgit.ideas.infrastructure.client.ReputationClient;
import com.redgit.ideas.infrastructure.entities.Contribution;
import com.redgit.ideas.infrastructure.entities.ContributionStatus;
import com.redgit.ideas.infrastructure.entities.Idea;
import com.redgit.ideas.infrastructure.repository.ContributionRepository;
import com.redgit.ideas.infrastructure.repository.IdeaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContributionService {

    private final ContributionRepository contributionRepository;
    private final IdeaRepository ideaRepository;
    private final ReputationClient reputationClient;

    public Contribution submit(String ideaId, String contributorId, ContributionCreateDTO dto) {
        if (!ideaRepository.existsById(ideaId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ideia não encontrada: " + ideaId);
        }

        Contribution contribution = new Contribution();
        contribution.setIdeaId(ideaId);
        contribution.setContributorId(contributorId);
        contribution.setDiff(dto.diff());
        contribution.setStatus(ContributionStatus.PENDING);
        contribution.setCreatedAt(LocalDateTime.now());

        return contributionRepository.save(contribution);
    }

    public List<ContributionDTO> listByIdea(String ideaId) {
        return contributionRepository.findByIdeaId(ideaId)
                .stream()
                .map(ContributionDTO::from)
                .toList();
    }

    public void accept(String contributionId, String requestingUserId) {
        Contribution contribution = contributionRepository.findById(contributionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Contribuição não encontrada: " + contributionId));

        Idea idea = ideaRepository.findById(contribution.getIdeaId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ideia não encontrada: " + contribution.getIdeaId()));

        if (!idea.getAuthorId().equals(requestingUserId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Apenas o autor da ideia pode aceitar contribuições");
        }

        if (contribution.getStatus() != ContributionStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Contribuição não está pendente");
        }

        contribution.setStatus(ContributionStatus.ACCEPTED);
        contribution.setReviewedAt(LocalDateTime.now());
        contributionRepository.save(contribution);

        reputationClient.notifyContributionAccepted(contribution.getContributorId(), contributionId);
    }

    public void reject(String contributionId, String requestingUserId) {
        Contribution contribution = contributionRepository.findById(contributionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Contribuição não encontrada: " + contributionId));

        Idea idea = ideaRepository.findById(contribution.getIdeaId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ideia não encontrada: " + contribution.getIdeaId()));

        if (!idea.getAuthorId().equals(requestingUserId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Apenas o autor da ideia pode rejeitar contribuições");
        }

        if (contribution.getStatus() != ContributionStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Contribuição não está pendente");
        }

        contribution.setStatus(ContributionStatus.REJECTED);
        contribution.setReviewedAt(LocalDateTime.now());
        contributionRepository.save(contribution);
    }
}
