package ru.yandex.service;

import org.springframework.scheduling.annotation.Async;
import ru.yandex.model.Post;
import ru.yandex.repository.PostRepository;
import ru.yandex.repository.dto.SearchResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Сервис по управлению постами
 */
public interface PostService {
    @Async
    CompletableFuture<SearchResult> getPosts(
            String search,
            int pageNumber,
            int pageSize
    );

    @Async
    CompletableFuture<Post> getPost(
            int id
    );

    @Async
    CompletableFuture<Post> addPost(
            String title,
            String text,
            List<String> tags
    );

    @Async
    CompletableFuture<Post> editPost(
            int id,
            String title,
            String text,
            List<String> tags
    );

    @Async
    CompletableFuture<Void> deletePost(
            int id
    );

    @Async
    CompletableFuture<Integer> likeIncrease(
            int id
    );
}

class ImplementedPostService implements PostService {

    private final PostRepository postRepository;

    public ImplementedPostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public CompletableFuture<SearchResult> getPosts(String search, int pageNumber, int pageSize) {
        return postRepository.getPosts(search, pageNumber, pageSize);
    }

    @Override
    public CompletableFuture<Post> getPost(int id) {
        return postRepository.getPost(id);
    }

    @Override
    public CompletableFuture<Post> addPost(String title, String text, List<String> tags) {
        return postRepository.addPost(title, text, tags);
    }

    @Override
    public CompletableFuture<Post> editPost(int id, String title, String text, List<String> tags) {
        return postRepository.editPost(id, title, text, tags);
    }

    @Override
    public CompletableFuture<Void> deletePost(int id) {
        return postRepository.deletePost(id);
    }

    @Override
    public CompletableFuture<Integer> likeIncrease(int id) {
        return postRepository.likeIncrease(id);
    }
}
