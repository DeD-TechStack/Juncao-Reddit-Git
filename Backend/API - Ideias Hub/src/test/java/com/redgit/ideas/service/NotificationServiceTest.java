package com.redgit.ideas.service;

import com.redgit.ideas.controller.dto.NotificationDTO;
import com.redgit.ideas.infrastructure.entities.Notification;
import com.redgit.ideas.infrastructure.entities.NotificationType;
import com.redgit.ideas.infrastructure.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Tests")
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testNotification = new Notification();
        testNotification.setId("notif-1");
        testNotification.setRecipientId("author@test.com");
        testNotification.setActorId("contributor@test.com");
        testNotification.setType(NotificationType.CONTRIBUTION_SUBMITTED);
        testNotification.setMessage("Nova contribuição recebida para sua ideia.");
        testNotification.setTargetType("CONTRIBUTION");
        testNotification.setTargetId("contrib-1");
        testNotification.setCreatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("createContributionSubmittedNotification Tests")
    class CreateNotificationTests {

        @Test
        @DisplayName("Deve salvar notificação com recipientId como autor da ideia")
        void createNotification_shouldSaveWithRecipientAsIdeaAuthor() {
            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            when(notificationRepository.save(captor.capture())).thenReturn(testNotification);

            notificationService.createContributionSubmittedNotification(
                    "author@test.com", "contributor@test.com", "contrib-1", "idea-1");

            assertEquals("author@test.com", captor.getValue().getRecipientId());
        }

        @Test
        @DisplayName("Deve salvar notificação com actorId como contribuidor")
        void createNotification_shouldSaveWithActorAsContributor() {
            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            when(notificationRepository.save(captor.capture())).thenReturn(testNotification);

            notificationService.createContributionSubmittedNotification(
                    "author@test.com", "contributor@test.com", "contrib-1", "idea-1");

            assertEquals("contributor@test.com", captor.getValue().getActorId());
        }

        @Test
        @DisplayName("Deve salvar notificação com targetId como id da contribuição")
        void createNotification_shouldSaveWithContributionIdAsTargetId() {
            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            when(notificationRepository.save(captor.capture())).thenReturn(testNotification);

            notificationService.createContributionSubmittedNotification(
                    "author@test.com", "contributor@test.com", "contrib-1", "idea-1");

            assertEquals("contrib-1", captor.getValue().getTargetId());
            assertEquals("CONTRIBUTION", captor.getValue().getTargetType());
            assertEquals(NotificationType.CONTRIBUTION_SUBMITTED, captor.getValue().getType());
        }
    }

    @Nested
    @DisplayName("listMyNotifications Tests")
    class ListMyNotificationsTests {

        @Test
        @DisplayName("Deve retornar notificações do usuário atual")
        void listMyNotifications_shouldReturnUserNotifications() {
            when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc("author@test.com"))
                    .thenReturn(List.of(testNotification));

            List<NotificationDTO> result = notificationService.listMyNotifications("author@test.com");

            assertEquals(1, result.size());
            assertEquals("notif-1", result.get(0).getId());
            assertEquals("author@test.com", result.get(0).getRecipientId());
            assertEquals("contributor@test.com", result.get(0).getActorId());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há notificações")
        void listMyNotifications_noNotifications_shouldReturnEmpty() {
            when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc("author@test.com"))
                    .thenReturn(List.of());

            List<NotificationDTO> result = notificationService.listMyNotifications("author@test.com");

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("markAsRead Tests")
    class MarkAsReadTests {

        @Test
        @DisplayName("Deve definir readAt ao marcar como lida")
        void markAsRead_shouldSetReadAt() {
            when(notificationRepository.findById("notif-1")).thenReturn(Optional.of(testNotification));
            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            when(notificationRepository.save(captor.capture())).thenReturn(testNotification);

            notificationService.markAsRead("notif-1", "author@test.com");

            assertNotNull(captor.getValue().getReadAt());
        }

        @Test
        @DisplayName("Deve ser idempotente quando notificação já está lida")
        void markAsRead_alreadyRead_shouldNotSaveAgain() {
            testNotification.setReadAt(LocalDateTime.now().minusHours(1));
            when(notificationRepository.findById("notif-1")).thenReturn(Optional.of(testNotification));

            notificationService.markAsRead("notif-1", "author@test.com");

            verify(notificationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar 403 quando não é o destinatário")
        void markAsRead_byNonRecipient_shouldThrow403() {
            when(notificationRepository.findById("notif-1")).thenReturn(Optional.of(testNotification));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> notificationService.markAsRead("notif-1", "other@test.com"));

            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
            verify(notificationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar 404 quando notificação não existe")
        void markAsRead_notFound_shouldThrow404() {
            when(notificationRepository.findById("missing")).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> notificationService.markAsRead("missing", "author@test.com"));

            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }

        @Test
        @DisplayName("Deve marcar notificação como lida com read=true no DTO")
        void markAsRead_shouldExposeReadTrueInDTO() {
            testNotification.setReadAt(LocalDateTime.now());

            NotificationDTO dto = NotificationDTO.from(testNotification);

            assertTrue(dto.isRead());
            assertNotNull(dto.getReadAt());
        }
    }
}
