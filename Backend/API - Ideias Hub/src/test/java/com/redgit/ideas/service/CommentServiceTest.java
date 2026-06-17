package com.redgit.ideas.service;

import com.redgit.ideas.controller.dto.CommentCreateDTO;
import com.redgit.ideas.controller.dto.CommentDTO;
import com.redgit.ideas.infrastructure.entities.Comment;
import com.redgit.ideas.infrastructure.repository.CommentRepository;
import com.redgit.ideas.infrastructure.repository.IdeaRepository;
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
@DisplayName("CommentService Tests")
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private IdeaRepository ideaRepository;

    @InjectMocks
    private CommentService commentService;

    private Comment rootComment;
    private Comment replyComment;

    @BeforeEach
    void setUp() {
        rootComment = new Comment();
        rootComment.setId("comment-1");
        rootComment.setIdeaId("idea-1");
        rootComment.setAuthorId("user@test.com");
        rootComment.setContent("Root comment");
        rootComment.setCreatedAt(LocalDateTime.now());

        replyComment = new Comment();
        replyComment.setId("comment-2");
        replyComment.setIdeaId("idea-1");
        replyComment.setAuthorId("other@test.com");
        replyComment.setParentId("comment-1");
        replyComment.setContent("Reply comment");
        replyComment.setCreatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("createComment Tests")
    class CreateCommentTests {

        @Test
        @DisplayName("Deve criar comentário raiz para ideia existente")
        void createComment_rootComment_shouldSaveAndReturn() {
            when(ideaRepository.existsById("idea-1")).thenReturn(true);
            when(commentRepository.save(any(Comment.class))).thenReturn(rootComment);

            CommentCreateDTO dto = new CommentCreateDTO("Root comment", null);
            Comment result = commentService.createComment("idea-1", "user@test.com", dto);

            assertNotNull(result);
            assertEquals("comment-1", result.getId());
            assertEquals("Root comment", result.getContent());
            verify(commentRepository, times(1)).save(any(Comment.class));
        }

        @Test
        @DisplayName("Deve criar resposta para comentário pai válido")
        void createComment_reply_shouldSetParentId() {
            when(ideaRepository.existsById("idea-1")).thenReturn(true);
            when(commentRepository.findById("comment-1")).thenReturn(Optional.of(rootComment));
            when(commentRepository.save(any(Comment.class))).thenReturn(replyComment);

            CommentCreateDTO dto = new CommentCreateDTO("Reply comment", "comment-1");
            Comment result = commentService.createComment("idea-1", "other@test.com", dto);

            assertNotNull(result);
            assertEquals("comment-1", result.getParentId());
        }

        @Test
        @DisplayName("Deve rejeitar criação quando ideia não existe")
        void createComment_ideaNotFound_shouldThrow404() {
            when(ideaRepository.existsById("non-existent")).thenReturn(false);

            CommentCreateDTO dto = new CommentCreateDTO("Content", null);
            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> commentService.createComment("non-existent", "user@test.com", dto));

            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
            verify(commentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve rejeitar resposta quando comentário pai não existe")
        void createComment_parentNotFound_shouldThrow404() {
            when(ideaRepository.existsById("idea-1")).thenReturn(true);
            when(commentRepository.findById("missing-parent")).thenReturn(Optional.empty());

            CommentCreateDTO dto = new CommentCreateDTO("Reply", "missing-parent");
            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> commentService.createComment("idea-1", "user@test.com", dto));

            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
            verify(commentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve rejeitar resposta quando comentário pai pertence a outra ideia")
        void createComment_parentFromDifferentIdea_shouldThrow400() {
            Comment foreignParent = new Comment();
            foreignParent.setId("comment-99");
            foreignParent.setIdeaId("idea-999");
            foreignParent.setAuthorId("user@test.com");
            foreignParent.setContent("Foreign comment");

            when(ideaRepository.existsById("idea-1")).thenReturn(true);
            when(commentRepository.findById("comment-99")).thenReturn(Optional.of(foreignParent));

            CommentCreateDTO dto = new CommentCreateDTO("Reply", "comment-99");
            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> commentService.createComment("idea-1", "user@test.com", dto));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
            verify(commentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve salvar comentário com campos corretos")
        void createComment_shouldSaveWithCorrectFields() {
            when(ideaRepository.existsById("idea-1")).thenReturn(true);
            ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
            when(commentRepository.save(captor.capture())).thenReturn(rootComment);

            CommentCreateDTO dto = new CommentCreateDTO("My comment", null);
            commentService.createComment("idea-1", "user@test.com", dto);

            Comment saved = captor.getValue();
            assertEquals("idea-1", saved.getIdeaId());
            assertEquals("user@test.com", saved.getAuthorId());
            assertEquals("My comment", saved.getContent());
            assertNull(saved.getParentId());
            assertNotNull(saved.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("softDelete Tests")
    class SoftDeleteTests {

        @Test
        @DisplayName("Deve alterar conteúdo para 'removido' ao deletar")
        void softDelete_shouldSetContentToRemovido() {
            when(commentRepository.findById("comment-1")).thenReturn(Optional.of(rootComment));
            ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
            when(commentRepository.save(captor.capture())).thenReturn(rootComment);

            commentService.softDelete("comment-1", "user@test.com");

            assertEquals("removido", captor.getValue().getContent());
        }

        @Test
        @DisplayName("Deve definir deletedAt ao deletar")
        void softDelete_shouldSetDeletedAt() {
            when(commentRepository.findById("comment-1")).thenReturn(Optional.of(rootComment));
            ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
            when(commentRepository.save(captor.capture())).thenReturn(rootComment);

            commentService.softDelete("comment-1", "user@test.com");

            assertNotNull(captor.getValue().getDeletedAt());
        }

        @Test
        @DisplayName("Deve preservar parentId após deleção")
        void softDelete_shouldPreserveParentId() {
            when(commentRepository.findById("comment-2")).thenReturn(Optional.of(replyComment));
            ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
            when(commentRepository.save(captor.capture())).thenReturn(replyComment);

            commentService.softDelete("comment-2", "other@test.com");

            assertEquals("comment-1", captor.getValue().getParentId());
        }

        @Test
        @DisplayName("Deve lançar 403 quando não é o autor")
        void softDelete_byNonAuthor_shouldThrow403() {
            when(commentRepository.findById("comment-1")).thenReturn(Optional.of(rootComment));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> commentService.softDelete("comment-1", "other@test.com"));

            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
            verify(commentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar 404 quando comentário não existe")
        void softDelete_commentNotFound_shouldThrow404() {
            when(commentRepository.findById("missing")).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> commentService.softDelete("missing", "user@test.com"));

            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }

        @Test
        @DisplayName("Não deve modificar comentário já deletado")
        void softDelete_alreadyDeleted_shouldNotSaveAgain() {
            rootComment.setContent("removido");
            rootComment.setDeletedAt(LocalDateTime.now().minusDays(1));

            when(commentRepository.findById("comment-1")).thenReturn(Optional.of(rootComment));

            commentService.softDelete("comment-1", "user@test.com");

            verify(commentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("listComments Tests")
    class ListCommentsTests {

        @Test
        @DisplayName("Deve retornar comentários raiz e respostas em árvore")
        void listComments_shouldBuildTree() {
            when(commentRepository.findByIdeaId("idea-1"))
                    .thenReturn(List.of(rootComment, replyComment));

            List<CommentDTO> result = commentService.listComments("idea-1");

            assertEquals(1, result.size());
            assertEquals("comment-1", result.get(0).getId());
            assertEquals(1, result.get(0).getReplies().size());
            assertEquals("comment-2", result.get(0).getReplies().get(0).getId());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há comentários")
        void listComments_noComments_shouldReturnEmpty() {
            when(commentRepository.findByIdeaId("idea-1")).thenReturn(List.of());

            List<CommentDTO> result = commentService.listComments("idea-1");

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Deve marcar comentário deletado com deleted=true")
        void listComments_deletedComment_shouldHaveDeletedFlag() {
            rootComment.setContent("removido");
            rootComment.setDeletedAt(LocalDateTime.now());

            when(commentRepository.findByIdeaId("idea-1")).thenReturn(List.of(rootComment));

            List<CommentDTO> result = commentService.listComments("idea-1");

            assertTrue(result.get(0).isDeleted());
            assertEquals("removido", result.get(0).getContent());
        }

        @Test
        @DisplayName("Filho de comentário deletado deve aparecer na árvore")
        void listComments_replyUnderDeletedParent_shouldStillAppear() {
            rootComment.setContent("removido");
            rootComment.setDeletedAt(LocalDateTime.now());

            when(commentRepository.findByIdeaId("idea-1"))
                    .thenReturn(List.of(rootComment, replyComment));

            List<CommentDTO> result = commentService.listComments("idea-1");

            assertEquals(1, result.size());
            assertTrue(result.get(0).isDeleted());
            assertEquals(1, result.get(0).getReplies().size());
            assertEquals("comment-2", result.get(0).getReplies().get(0).getId());
        }
    }
}
