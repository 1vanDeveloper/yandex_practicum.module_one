package ru.yandex.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.controller.dto.AddCommentRequest;
import ru.yandex.controller.dto.CommentResponse;
import ru.yandex.controller.dto.CommentsResponse;
import ru.yandex.controller.dto.UpdateCommentRequest;
import ru.yandex.model.Comment;
import ru.yandex.service.CommentService;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@Validated
public class CommentController {
    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * Получение всех комментариев к посту
     * @param postId идентификатор поста
     * @return все комметрии поста
     */
    @Async
    @GetMapping()
    @ResponseBody
    public CompletableFuture<CommentsResponse> getComments(@PathVariable(name = "postId") @Min(1) int postId) {
        return commentService.getComments(postId).thenApplyAsync(comments -> {
            var response = new CommentsResponse();
            response.addAll(comments.stream().map(CommentController::convert).toList());
            return response;
        });
    }

    /**
     * Получение комментария к посту
     * @param postId идентификатор поста
     * @param commentId идентификатор комментария
     * @return комментарий к посту
     */
    @Async
    @GetMapping("/{commentId}")
    @ResponseBody
    public CompletableFuture<CommentResponse> getComment(
            @PathVariable(name = "postId") @Min(1) int postId,
            @PathVariable(name = "commentId") @Min(1) long commentId) {
        return commentService.getComment(commentId)
                .thenApplyAsync(CommentController::convert);
    }

    /**
     * Добавление комментария к посту
     * @param postId идентификатор поста
     * @return новый комментарий к посту
     */
    @Async
    @PostMapping
    @ResponseBody
    public CompletableFuture<CommentResponse> addComment(
            @PathVariable(name = "postId") @Min(1) int postId,
            @RequestBody @Valid AddCommentRequest request) {
        return commentService.addComment(request.postId(), request.text())
                .thenApplyAsync(CommentController::convert);
    }

    /**
     * Обновление комментария к посту
     * @param postId идентификатор поста
     * @param commentId идентификатор комментария
     * @return комментарий к посту
     */
    @Async
    @PutMapping("/{commentId}")
    @ResponseBody
    public CompletableFuture<CommentResponse> updateComment(
            @PathVariable(name = "postId") @Min(1) int postId,
            @PathVariable(name = "commentId") @Min(1) long commentId,
            @RequestBody @Valid UpdateCommentRequest request) {
        return commentService.updateComment(request.id(), request.text(), request.postId())
                .thenApplyAsync(CommentController::convert);
    }

    /**
     * Удаление комментария к посту
     * @param postId идентификатор поста
     * @param commentId идентификатор комментария
     */
    @Async
    @DeleteMapping("/{commentId}")
    @ResponseBody
    public CompletableFuture<Void> updateComment(
            @PathVariable(name = "postId") @Min(1) int postId,
            @PathVariable(name = "commentId") @Min(1) long commentId) {
        return commentService.deleteComment(commentId);
    }

    private static CommentResponse convert(Comment comment) {
        if (comment == null) {
            return null;
        }

        return new CommentResponse(
                comment.getId(),
                comment.getText(),
                comment.getPostId()
        );
    }
}
