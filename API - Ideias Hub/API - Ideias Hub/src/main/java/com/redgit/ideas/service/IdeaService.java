package com.redgit.ideas.service;

import com.redgit.ideas.controller.dto.IdeaCreateDTO;
import com.redgit.ideas.controller.dto.IdeaDTO;
import com.redgit.ideas.infrastructure.entities.Idea;
import com.redgit.ideas.infrastructure.repository.IdeaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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

    public List<Idea> getAllIdeas(){
        return ideaRepository.findAll();
    }

    public Idea findById(String id){
        return ideaRepository.findById(id).orElseThrow(
                () -> new RuntimeException("id não encontrado")
        );
    }

    public List<Idea> getIdeasByAuthor(String authorId){
        return ideaRepository.findByAuthorId(authorId);
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
