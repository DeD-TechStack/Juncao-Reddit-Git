package com.daniel.registry.reputation.controller;

import com.daniel.registry.reputation.dto.ReputationEventRequestDTO;
import com.daniel.registry.reputation.dto.ReputationEventResponseDTO;
import com.daniel.registry.reputation.dto.ReputationResponseDTO;
import com.daniel.registry.reputation.service.ReputationService;
import jakarta.validation.Valid;
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
        String userId = (String) auth.getPrincipal();
        return service.me(userId);
    }

    @PostMapping("/events")
    public ResponseEntity<ReputationEventResponseDTO> event(
            @RequestBody @Valid ReputationEventRequestDTO dto,
            Authentication auth) {
        String userId = (String) auth.getPrincipal();
        return ResponseEntity.ok(service.applyEvent(userId, dto.type()));
    }
}
