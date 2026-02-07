package ru.yandex.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.controller.dto.AddPostRequest;
import ru.yandex.controller.dto.EditPostRequest;
import ru.yandex.controller.dto.PostResponse;
import ru.yandex.controller.dto.GetPostsResponse;
import ru.yandex.model.Post;
import ru.yandex.repository.PostRepository;
import ru.yandex.repository.dto.SearchResult;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

/**
 * Контреллер по управлению постами
 */
@RestController
@RequestMapping("/api/")
public class PostController {

    private final PostRepository postRepository;

    @Autowired
    public PostController(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    /**
     * Получение списка постов по строке поиска
     * @param search строка поиска
     * @param pageNumber текущий номер страницы поиска
     * @param pageSize размер страницы поиска
     * @return результат поиска
     */
    @GetMapping("posts")
    @ResponseBody
    public GetPostsResponse getPosts(
            @RequestParam(name = "search") String search,
            @RequestParam(name = "pageNumber") int pageNumber,
            @RequestParam(name = "pageSize") int pageSize
    ) {
        search = URLDecoder.decode(search, StandardCharsets.UTF_8);
        SearchResult searchResult;
        try {
            searchResult = postRepository.getPosts(search, pageNumber, pageSize).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        var responsePosts = searchResult.posts().stream().map(PostController::convert).toList();
        return new GetPostsResponse(responsePosts, searchResult.hasPrev(), searchResult.hasNext(), searchResult.lastPage());
    }

    /**
     * Получение поста по идентификатору
     * @param id идентификатор поста
     * @return пост
     */
    @GetMapping("posts/{id}")
    @ResponseBody
    public PostResponse getPost(
            @PathVariable(name = "id") int id
    ) {
        Post post;
        try {
            post = postRepository.getPost(id).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return convert(post);
    }

    /**
     * Добавление поста
     */
    @PostMapping("posts")
    @ResponseBody
    public PostResponse addPost(@RequestBody AddPostRequest request) {
        Post post;
        try {
            post = postRepository.addPost(request.title(), request.text(), request.tags()).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return convert(post);
    }

    /**
     * Редактирование поста
     * @param id идентификатор поста
     * @return пост
     */
    @PutMapping("posts/{id}")
    @ResponseBody
    public PostResponse editPost(
            @PathVariable(name = "id") int id,
            @RequestBody EditPostRequest request
    ) {
        if (request.id() != id) {
            throw new RuntimeException("ids from path and body are not equal");
        }
        Post post;
        try {
            post = postRepository.editPost(request.id(), request.title(), request.text(), request.tags()).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return convert(post);
    }

    /**
     * Удаление поста
     * @param id идентификатор поста
     */
    @DeleteMapping("posts/{id}")
    @ResponseBody
    public void deletePost(
            @PathVariable(name = "id") int id) {
        try {
            postRepository.deletePost(id).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
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
