package com.redgit.ideas.infrastructure.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "contributions")
public class Contribution {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "idea_id", nullable = false)
    private String ideaId;

    @Column(name = "contributor_id", nullable = false)
    private String contributorId;

    @Column(nullable = false, length = 10000)
    private String diff;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContributionStatus status = ContributionStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
}
