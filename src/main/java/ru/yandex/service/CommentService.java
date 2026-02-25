package ru.yandex.service;

import org.springframework.scheduling.annotation.Async;
import ru.yandex.model.Comment;
import ru.yandex.repository.CommentRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Сервис по управлению комментариями
 */
public interface CommentService {
    @Async
    CompletableFuture<List<Comment>> getComments(int postId);

    @Async
    CompletableFuture<Comment> getComment(long commentId);

    @Async
    CompletableFuture<Comment> addComment(int postId, String text);

    @Async
    CompletableFuture<Comment> updateComment(long commentId, String text, int postId);

    @Async
    CompletableFuture<Void> deleteComment(long commentId);
}

class ImplementedCommentService implements CommentService {

    private final CommentRepository commentRepository;

    public ImplementedCommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override
    public CompletableFuture<List<Comment>> getComments(int postId) {
        return commentRepository.getComments(postId);
    }

    @Override
    public CompletableFuture<Comment> getComment(long commentId) {
        return commentRepository.getComment(commentId);
    }

    @Override
    public CompletableFuture<Comment> addComment(int postId, String text) {
        return commentRepository.addComment(postId, text);
    }

    @Override
    public CompletableFuture<Comment> updateComment(long commentId, String text, int postId) {
        return commentRepository.updateComment(commentId, text, postId);
    }

    @Override
    public CompletableFuture<Void> deleteComment(long commentId) {
        return commentRepository.deleteComment(commentId);
    }
}
