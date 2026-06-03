package com.redgit.ideas.service;

import com.redgit.ideas.controller.dto.IdeaCreateDTO;
import com.redgit.ideas.controller.dto.IdeaDTO;
import com.redgit.ideas.infrastructure.entities.Idea;
import com.redgit.ideas.infrastructure.repository.IdeaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class IdeaService {

    private final IdeaRepository ideaRepository;

    public Idea createIdea(IdeaCreateDTO dto, String authorEmail) {
        Idea idea = new Idea();
        idea.setTitle(dto.title());
        idea.setDescription(dto.description());
        idea.setAuthorId(authorEmail);
        idea.setCreatedAt(LocalDateTime.now());
        return ideaRepository.save(idea);
    }

    public Page<Idea> getAllIdeas(Pageable pageable) {
        return ideaRepository.findAll(pageable);
    }

    public Idea findById(String id){
        return ideaRepository.findById(id).orElseThrow(
                () -> new RuntimeException("id não encontrado")
        );
    }

    public Page<Idea> getIdeasByAuthor(String authorId, Pageable pageable) {
        return ideaRepository.findByAuthorId(authorId, pageable);
    }

    public Idea replaceIdea(String id, IdeaDTO ideaDTO){
        Idea existing = findById(id);

        existing.setTitle(ideaDTO.getTitle());
        existing.setDescription(ideaDTO.getDescription());

        return ideaRepository.save(existing);
    }

    public Idea updateIdea(String id, IdeaDTO ideaDTO) {
        Idea existing = findById(id);

        if (ideaDTO.getTitle() != null)
            existing.setTitle(ideaDTO.getTitle());

        if (ideaDTO.getDescription() != null)
            existing.setDescription(ideaDTO.getDescription());

        return ideaRepository.save(existing);
    }

    public void deleteIdeaById(String id) {
        ideaRepository.deleteById(id);
    }
}
