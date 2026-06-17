package com.redgit.ideas.service;

import com.redgit.ideas.controller.dto.NotificationDTO;
import com.redgit.ideas.infrastructure.entities.Notification;
import com.redgit.ideas.infrastructure.entities.NotificationType;
import com.redgit.ideas.infrastructure.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void createContributionSubmittedNotification(
            String recipientId, String actorId, String contributionId, String ideaId) {
        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setActorId(actorId);
        notification.setType(NotificationType.CONTRIBUTION_SUBMITTED);
        notification.setMessage("Nova contribuição recebida para sua ideia.");
        notification.setTargetType("CONTRIBUTION");
        notification.setTargetId(contributionId);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    public List<NotificationDTO> listMyNotifications(String currentUserId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(currentUserId)
                .stream()
                .map(NotificationDTO::from)
                .toList();
    }

    public void markAsRead(String notificationId, String currentUserId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Notificação não encontrada: " + notificationId));

        if (!notification.getRecipientId().equals(currentUserId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Apenas o destinatário pode marcar a notificação como lida");
        }

        if (notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }
}
