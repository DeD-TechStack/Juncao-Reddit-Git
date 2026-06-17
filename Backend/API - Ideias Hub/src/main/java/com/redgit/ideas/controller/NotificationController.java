package com.redgit.ideas.controller;

import com.redgit.ideas.controller.dto.NotificationDTO;
import com.redgit.ideas.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/me")
    public ResponseEntity<List<NotificationDTO>> listMyNotifications(
            @AuthenticationPrincipal String userEmail) {
        return ResponseEntity.ok(notificationService.listMyNotifications(userEmail));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable String notificationId,
            @AuthenticationPrincipal String userEmail) {
        notificationService.markAsRead(notificationId, userEmail);
        return ResponseEntity.noContent().build();
    }
}
