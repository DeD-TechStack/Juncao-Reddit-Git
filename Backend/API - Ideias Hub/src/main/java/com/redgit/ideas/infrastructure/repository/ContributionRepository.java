package com.redgit.ideas.infrastructure.repository;

import com.redgit.ideas.infrastructure.entities.Contribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContributionRepository extends JpaRepository<Contribution, String> {
    List<Contribution> findByIdeaId(String ideaId);
}
