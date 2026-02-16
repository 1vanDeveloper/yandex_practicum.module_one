package ru.yandex.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.controller.dto.AddPostRequest;
import ru.yandex.controller.dto.EditPostRequest;
import ru.yandex.controller.dto.PostResponse;
import ru.yandex.controller.dto.GetPostsResponse;
import ru.yandex.model.Post;
import ru.yandex.service.PostService;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * Контреллер по управлению постами
 */
@RestController
@RequestMapping("/api/")
@Validated
public class PostController {

    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    /**
     * Получение списка постов по строке поиска
     * @param search строка поиска
     * @param pageNumber текущий номер страницы поиска
     * @param pageSize размер страницы поиска
     * @return результат поиска
     */
    @Async
    @GetMapping("posts")
    @ResponseBody
    public CompletableFuture<GetPostsResponse> getPosts(
            @RequestParam(name = "search") String search,
            @RequestParam(name = "pageNumber") @Min(1) int pageNumber,
            @RequestParam(name = "pageSize") @Min(1) @Max(100) int pageSize
    ) {
        search = URLDecoder.decode(search, StandardCharsets.UTF_8);

        return postService.getPosts(search, pageNumber, pageSize)
                .thenApplyAsync(searchResult -> {
                    var responsePosts = searchResult.posts().stream().map(PostController::convert).toList();
                    return new GetPostsResponse(responsePosts, searchResult.hasPrev(), searchResult.hasNext(), searchResult.lastPage());
                });
    }

    /**
     * Получение поста по идентификатору
     * @param id идентификатор поста
     * @return пост
     */
    @Async
    @GetMapping("posts/{id}")
    @ResponseBody
    public CompletableFuture<PostResponse> getPost(
            @PathVariable(name = "id") @Min(1) int id
    ) {
        return postService.getPost(id).thenApplyAsync(PostController::convert);
    }

    /**
     * Добавление поста
     */
    @Async
    @PostMapping("posts")
    @ResponseBody
    public CompletableFuture<PostResponse> addPost(@Valid @RequestBody AddPostRequest request) {
        return postService.addPost(request.title(), request.text(), request.tags())
                .thenApplyAsync(PostController::convert);
    }

    /**
     * Редактирование поста
     * @param id идентификатор поста
     * @return пост
     */
    @Async
    @PutMapping("posts/{id}")
    @ResponseBody
    public CompletableFuture<PostResponse> editPost(
            @PathVariable(name = "id") @Min(1) int id,
            @Valid @RequestBody EditPostRequest request
    ) {
        if (request.id() != id) {
            throw new RuntimeException("ids from path and body are not equal");
        }

        return postService.editPost(request.id(), request.title(), request.text(), request.tags())
                .thenApplyAsync(PostController::convert);
    }

    /**
     * Удаление поста
     * @param id идентификатор поста
     */
    @Async
    @DeleteMapping("posts/{id}")
    @ResponseBody
    public CompletableFuture<Void> deletePost(
            @PathVariable(name = "id") @Min(1) int id) {
        return postService.deletePost(id);
    }

    /**
     * Добавление лайка к посту
     */
    @Async
    @PostMapping("posts/{id}/likes")
    @ResponseBody
    public CompletableFuture<Integer> likeIncrease(
            @PathVariable(name = "id") @Min(1) int id) {
        return postService.likeIncrease(id);
    }

    private static PostResponse convert(Post post) {
        if (post == null) {
            return null;
        }

        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getText(),
                post.getTags(),
                post.getLikesCount(),
                post.getCommentsCount()
        );
    }
}
