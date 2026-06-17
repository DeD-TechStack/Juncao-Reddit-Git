package com.redgit.ideas.controller;

import com.redgit.ideas.controller.dto.NotificationDTO;
import com.redgit.ideas.infrastructure.entities.NotificationType;
import com.redgit.ideas.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationController Tests")
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private NotificationDTO testNotificationDTO;

    @BeforeEach
    void setUp() {
        testNotificationDTO = new NotificationDTO();
        testNotificationDTO.setId("notif-1");
        testNotificationDTO.setRecipientId("author@test.com");
        testNotificationDTO.setActorId("contributor@test.com");
        testNotificationDTO.setType(NotificationType.CONTRIBUTION_SUBMITTED);
        testNotificationDTO.setMessage("Nova contribuição recebida para sua ideia.");
        testNotificationDTO.setTargetType("CONTRIBUTION");
        testNotificationDTO.setTargetId("contrib-1");
        testNotificationDTO.setRead(false);
        testNotificationDTO.setCreatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("listMyNotifications Tests")
    class ListMyNotificationsTests {

        @Test
        @DisplayName("Deve retornar 200 com lista de notificações")
        void listMyNotifications_shouldReturn200() {
            when(notificationService.listMyNotifications("author@test.com"))
                    .thenReturn(List.of(testNotificationDTO));

            ResponseEntity<List<NotificationDTO>> response =
                    notificationController.listMyNotifications("author@test.com");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
            assertEquals("notif-1", response.getBody().get(0).getId());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há notificações")
        void listMyNotifications_empty_shouldReturn200WithEmptyList() {
            when(notificationService.listMyNotifications("author@test.com")).thenReturn(List.of());

            ResponseEntity<List<NotificationDTO>> response =
                    notificationController.listMyNotifications("author@test.com");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isEmpty());
        }
    }

    @Nested
    @DisplayName("markAsRead Tests")
    class MarkAsReadTests {

        @Test
        @DisplayName("Deve retornar 204 ao marcar notificação como lida")
        void markAsRead_shouldReturn204() {
            doNothing().when(notificationService).markAsRead("notif-1", "author@test.com");

            ResponseEntity<Void> response =
                    notificationController.markAsRead("notif-1", "author@test.com");

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            verify(notificationService, times(1)).markAsRead("notif-1", "author@test.com");
        }

        @Test
        @DisplayName("Deve propagar 403 quando não é o destinatário")
        void markAsRead_notRecipient_shouldPropagate403() {
            doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas o destinatário pode marcar a notificação como lida"))
                    .when(notificationService).markAsRead("notif-1", "other@test.com");

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> notificationController.markAsRead("notif-1", "other@test.com"));

            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        }

        @Test
        @DisplayName("Deve propagar 404 quando notificação não existe")
        void markAsRead_notFound_shouldPropagate404() {
            doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Notificação não encontrada"))
                    .when(notificationService).markAsRead("missing", "author@test.com");

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> notificationController.markAsRead("missing", "author@test.com"));

            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }
    }
}
