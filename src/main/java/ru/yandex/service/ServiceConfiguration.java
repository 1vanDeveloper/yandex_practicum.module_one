package ru.yandex.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.repository.*;

/**
 * Настройка сервисов
 */
@Configuration
public class ServiceConfiguration {
    /**
     * Сервис управления постами
     */
    @Bean
    public PostService postService(PostRepository postRepository) {
        return new ImplementedPostService(postRepository);
    }

    /**
     * Сервис управления изображениями
     */
    @Bean
    public ImageService imageService(ImageRepository imageRepository) {
        return new ImplementedImageService(imageRepository);
    }

    /**
     * Сервис управления комментариями
     */
    @Bean
    public CommentService commentService(CommentRepository commentRepository) {
        return new ImplementedCommentService(commentRepository);
    }
}
