package com.redgit.ideas.controller.dto;

import com.redgit.ideas.infrastructure.entities.Contribution;
import com.redgit.ideas.infrastructure.entities.ContributionStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ContributionDTO {

    private String id;
    private String ideaId;
    private String contributorId;
    private String diff;
    private ContributionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;

    public ContributionDTO() {}

    public static ContributionDTO from(Contribution contribution) {
        ContributionDTO dto = new ContributionDTO();
        dto.setId(contribution.getId());
        dto.setIdeaId(contribution.getIdeaId());
        dto.setContributorId(contribution.getContributorId());
        dto.setDiff(contribution.getDiff());
        dto.setStatus(contribution.getStatus());
        dto.setCreatedAt(contribution.getCreatedAt());
        dto.setReviewedAt(contribution.getReviewedAt());
        return dto;
    }
}
