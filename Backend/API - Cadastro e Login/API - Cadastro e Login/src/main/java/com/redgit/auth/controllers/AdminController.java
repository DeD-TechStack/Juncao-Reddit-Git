package com.redgit.auth.controllers;

import com.redgit.auth.controllers.DTO.AdminStatsDTO;
import com.redgit.auth.controllers.DTO.UserDTO;
import com.redgit.auth.controllers.DTO.ChangeRoleDTO;
import com.redgit.auth.infrastructure.entity.User;
import com.redgit.auth.infrastructure.redis.RateLimitService;
import com.redgit.auth.service.UserService;
import com.redgit.auth.infrastructure.entity.UserRole;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    private final UserService userService;
    private final RateLimitService rateLimitService;

    @GetMapping("/users")
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<UserDTO> users = userService.findAll(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable UUID id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(new UserDTO(user));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<UserDTO> changeUserRole(
            @PathVariable UUID id,
            @RequestBody @Valid ChangeRoleDTO dto,
            @AuthenticationPrincipal String adminEmail) {
        User user = userService.changeRole(id, dto.getRole());
        audit.info("[AUDIT] CHANGE_ROLE | executadoPor={} | alvo={} | novaRole={} | ts={}", adminEmail, id, dto.getRole(), Instant.now());
        return ResponseEntity.ok(new UserDTO(user));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal String adminEmail) {
        userService.delete(id);
        audit.info("[AUDIT] DELETE_USER | executadoPor={} | alvo={} | ts={}", adminEmail, id, Instant.now());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{id}/lock")
    public ResponseEntity<Void> lockUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal String adminEmail) {
        userService.lockAccount(id);
        audit.info("[AUDIT] LOCK_USER | executadoPor={} | alvo={} | ts={}", adminEmail, id, Instant.now());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{id}/unlock")
    public ResponseEntity<Void> unlockUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal String adminEmail) {
        userService.unlockAccount(id);
        audit.info("[AUDIT] UNLOCK_USER | executadoPor={} | alvo={} | ts={}", adminEmail, id, Instant.now());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{id}/disable")
    public ResponseEntity<Void> disableUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal String adminEmail) {
        userService.disableAccount(id);
        audit.info("[AUDIT] DISABLE_USER | executadoPor={} | alvo={} | ts={}", adminEmail, id, Instant.now());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{id}/enable")
    public ResponseEntity<Void> enableUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal String adminEmail) {
        userService.enableAccount(id);
        audit.info("[AUDIT] ENABLE_USER | executadoPor={} | alvo={} | ts={}", adminEmail, id, Instant.now());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDTO> getStats() {
        AdminStatsDTO stats = new AdminStatsDTO();
        stats.setTotalUsers(userService.countUsers());
        stats.setTotalAdmins(userService.countByRole(UserRole.ADMIN));
        stats.setTotalRegularUsers(userService.countByRole(UserRole.USER));
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/users/unblock-ratelimit")
    public ResponseEntity<Map<String, String>> unblockUserRateLimit(
            @RequestBody Map<String, String> request) {

        String email = request.get("email");

        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email é obrigatório"));
        }

        rateLimitService.unblockUser(email);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Usuário desbloqueado com sucesso");
        response.put("email", email);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/ratelimit-status")
    public ResponseEntity<Map<String, Object>> getRateLimitStatus(
            @RequestParam String email) {

        Map<String, Object> status = new HashMap<>();
        status.put("email", email);
        status.put("isBlocked", rateLimitService.isBlocked(email));
        status.put("remainingAttempts", rateLimitService.getRemainingAttempts(email));
        status.put("blockTimeRemaining", rateLimitService.getBlockTimeRemaining(email));

        return ResponseEntity.ok(status);
    }
}