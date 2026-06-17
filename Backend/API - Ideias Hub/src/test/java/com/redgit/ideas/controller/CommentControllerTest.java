package com.redgit.ideas.controller;

import com.redgit.ideas.controller.dto.CommentCreateDTO;
import com.redgit.ideas.controller.dto.CommentDTO;
import com.redgit.ideas.infrastructure.entities.Comment;
import com.redgit.ideas.service.CommentService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentController Tests")
class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController commentController;

    private Comment testComment;
    private CommentDTO testCommentDTO;

    @BeforeEach
    void setUp() {
        testComment = new Comment();
        testComment.setId("comment-1");
        testComment.setIdeaId("idea-1");
        testComment.setAuthorId("user@test.com");
        testComment.setContent("Test comment");
        testComment.setCreatedAt(LocalDateTime.now());

        testCommentDTO = CommentDTO.from(testComment);
    }

    @Nested
    @DisplayName("createComment Tests")
    class CreateCommentTests {

        @Test
        @DisplayName("Deve retornar 201 ao criar comentário")
        void createComment_shouldReturn201() {
            when(commentService.createComment(eq("idea-1"), eq("user@test.com"), any(CommentCreateDTO.class)))
                    .thenReturn(testComment);

            CommentCreateDTO dto = new CommentCreateDTO("Test comment", null);
            ResponseEntity<CommentDTO> response =
                    commentController.createComment("idea-1", dto, "user@test.com");

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("comment-1", response.getBody().getId());
            assertEquals("Test comment", response.getBody().getContent());
        }

        @Test
        @DisplayName("Deve propagar 404 quando ideia não existe")
        void createComment_ideaNotFound_shouldPropagate404() {
            when(commentService.createComment(eq("missing"), eq("user@test.com"), any()))
                    .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Ideia não encontrada"));

            CommentCreateDTO dto = new CommentCreateDTO("Content", null);
            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> commentController.createComment("missing", dto, "user@test.com"));

            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }

        @Test
        @DisplayName("Deve propagar 400 quando comentário pai pertence a outra ideia")
        void createComment_invalidParent_shouldPropagate400() {
            when(commentService.createComment(eq("idea-1"), eq("user@test.com"), any()))
                    .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comentário pai pertence a outra ideia"));

            CommentCreateDTO dto = new CommentCreateDTO("Reply", "foreign-comment");
            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> commentController.createComment("idea-1", dto, "user@test.com"));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        }
    }

    @Nested
    @DisplayName("listComments Tests")
    class ListCommentsTests {

        @Test
        @DisplayName("Deve retornar 200 com lista de comentários")
        void listComments_shouldReturn200() {
            when(commentService.listComments("idea-1")).thenReturn(List.of(testCommentDTO));

            ResponseEntity<List<CommentDTO>> response =
                    commentController.listComments("idea-1");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há comentários")
        void listComments_noComments_shouldReturnEmptyList() {
            when(commentService.listComments("idea-1")).thenReturn(List.of());

            ResponseEntity<List<CommentDTO>> response =
                    commentController.listComments("idea-1");

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isEmpty());
        }
    }

    @Nested
    @DisplayName("deleteComment Tests")
    class DeleteCommentTests {

        @Test
        @DisplayName("Deve retornar 204 ao deletar comentário próprio")
        void deleteComment_byAuthor_shouldReturn204() {
            doNothing().when(commentService).softDelete("comment-1", "user@test.com");

            ResponseEntity<Void> response =
                    commentController.deleteComment("comment-1", "user@test.com");

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            verify(commentService, times(1)).softDelete("comment-1", "user@test.com");
        }

        @Test
        @DisplayName("Deve propagar 403 quando não é o autor")
        void deleteComment_byNonAuthor_shouldPropagate403() {
            doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas o autor pode deletar"))
                    .when(commentService).softDelete("comment-1", "other@test.com");

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> commentController.deleteComment("comment-1", "other@test.com"));

            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        }

        @Test
        @DisplayName("Deve propagar 404 quando comentário não existe")
        void deleteComment_notFound_shouldPropagate404() {
            doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Comentário não encontrado"))
                    .when(commentService).softDelete("missing", "user@test.com");

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> commentController.deleteComment("missing", "user@test.com"));

            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }
    }
}
