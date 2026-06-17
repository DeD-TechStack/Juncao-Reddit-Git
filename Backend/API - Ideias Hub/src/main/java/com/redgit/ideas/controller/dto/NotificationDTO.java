package com.redgit.ideas.controller.dto;

import com.redgit.ideas.infrastructure.entities.Notification;
import com.redgit.ideas.infrastructure.entities.NotificationType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDTO {

    private String id;
    private String recipientId;
    private String actorId;
    private NotificationType type;
    private String message;
    private String targetType;
    private String targetId;
    private boolean read;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;

    public NotificationDTO() {}

    public static NotificationDTO from(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setRecipientId(notification.getRecipientId());
        dto.setActorId(notification.getActorId());
        dto.setType(notification.getType());
        dto.setMessage(notification.getMessage());
        dto.setTargetType(notification.getTargetType());
        dto.setTargetId(notification.getTargetId());
        dto.setRead(notification.getReadAt() != null);
        dto.setReadAt(notification.getReadAt());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }
}
