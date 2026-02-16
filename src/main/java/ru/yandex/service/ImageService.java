package ru.yandex.service;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.repository.ImageRepository;

import java.util.concurrent.CompletableFuture;

/**
 * Сервис по управлению картинками
 */
public interface ImageService {
    @Async
    CompletableFuture<Integer> saveImage(MultipartFile image, int postId);

    @Async
    CompletableFuture<Pair<Resource, String>> getImage(int postId);
}

class ImplementedImageService implements ImageService {

    private final ImageRepository imageRepository;

    public ImplementedImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Override
    public CompletableFuture<Integer> saveImage(MultipartFile image, int postId) {
        return imageRepository.saveImage(image, postId);
    }

    @Override
    public CompletableFuture<Pair<Resource, String>> getImage(int postId) {
        return imageRepository.getImage(postId);
    }
}
