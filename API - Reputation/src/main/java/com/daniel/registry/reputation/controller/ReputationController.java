package com.daniel.registry.reputation.controller;

import com.daniel.registry.reputation.dto.ReputationEventResponseDTO;
import com.daniel.registry.reputation.dto.ReputationResponseDTO;
import com.daniel.registry.reputation.infrastructure.entity.ReputationEventType;
import com.daniel.registry.reputation.service.ReputationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reputation")
public class ReputationController {

    private final ReputationService service;

    public ReputationController(ReputationService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public ReputationResponseDTO me(Authentication auth) {
        String email = (String) auth.getPrincipal();
        return service.me(email);
    }

    @PostMapping("/events/{type}")
    public ResponseEntity<ReputationEventResponseDTO> event(@PathVariable ReputationEventType type, Authentication auth) {
        String email = (String) auth.getPrincipal();
        return ResponseEntity.ok(service.applyEvent(email, type));
    }
}
