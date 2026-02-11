package ru.yandex.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.controller.dto.AddCommentRequest;
import ru.yandex.controller.dto.CommentResponse;
import ru.yandex.controller.dto.CommentsResponse;
import ru.yandex.controller.dto.UpdateCommentRequest;
import ru.yandex.model.Comment;
import ru.yandex.repository.CommentRepository;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {
    private final CommentRepository commentRepository;

    @Autowired
    public CommentController(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    /**
     * Получение всех комментариев к посту
     * @param postId идентификатор поста
     * @return все комметрии поста
     */
    @GetMapping()
    @ResponseBody
    public CommentsResponse getComments(@PathVariable(name = "postId") int postId)
            throws ExecutionException, InterruptedException {
        var comments = commentRepository.getComments(postId).get();
        var response = new CommentsResponse();
        response.addAll(comments.stream().map(CommentController::convert).toList());
        return response;
    }

    /**
     * Получение комментария к посту
     * @param postId идентификатор поста
     * @param commentId идентификатор комментария
     * @return комментарий к посту
     */
    @GetMapping("/{commentId}")
    @ResponseBody
    public CommentResponse getComment(
            @PathVariable(name = "postId") int postId,
            @PathVariable(name = "commentId") int commentId)
            throws ExecutionException, InterruptedException {
        var comment = commentRepository.getComment(commentId).get();
        return convert(comment);
    }

    /**
     * Добавление комментария к посту
     * @param postId идентификатор поста
     * @return новый комментарий к посту
     */
    @PostMapping()
    @ResponseBody
    public CommentResponse addComment(
            @PathVariable(name = "postId") int postId,
            @RequestBody AddCommentRequest request)
            throws ExecutionException, InterruptedException {
        var comment = commentRepository.addComment(request.postId(), request.text()).get();
        return convert(comment);
    }

    /**
     * Обновление комментария к посту
     * @param postId идентификатор поста
     * @param commentId идентификатор комментария
     * @return комментарий к посту
     */
    @PutMapping("/{commentId}")
    @ResponseBody
    public CommentResponse updateComment(
            @PathVariable(name = "postId") int postId,
            @PathVariable(name = "commentId") long commentId,
            @RequestBody UpdateCommentRequest request)
            throws ExecutionException, InterruptedException {
        var comment = commentRepository.updateComment(request.id(), request.text(), request.postId()).get();
        return convert(comment);
    }

    /**
     * Удаление комментария к посту
     * @param postId идентификатор поста
     * @param commentId идентификатор комментария
     */
    @DeleteMapping("/{commentId}")
    @ResponseBody
    public void updateComment(
            @PathVariable(name = "postId") int postId,
            @PathVariable(name = "commentId") long commentId)
            throws ExecutionException, InterruptedException {
        commentRepository.deleteComment(commentId).get();
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
