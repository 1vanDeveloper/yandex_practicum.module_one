package ru.yandex.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.controller.dto.GetPostResponse;
import ru.yandex.controller.dto.GetPostsResponse;
import ru.yandex.model.Post;
import ru.yandex.repository.PostRepository;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
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
        List<Post> posts;
        try {
            posts = postRepository.getPosts(search, pageNumber, pageSize).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return new GetPostsResponse(posts.stream().map(p ->
                new GetPostResponse(
                        p.getId(),
                        p.getTitle(),
                        p.getText(),
                        p.getTags(),
                        p.getLikesCount(),
                        p.getCommentsCount()
                )
        ).toList());
    }
}
