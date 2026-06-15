package com.redgit.ideas.infrastructure.repository;

import com.redgit.ideas.infrastructure.entities.Idea;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdeaRepository extends JpaRepository<Idea, String> {
    Page<Idea> findByAuthorId(String authorId, Pageable pageable);
}
