package ru.yandex.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.service.ImageService;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/posts")
public class ImageController {

    private final ImageService imageService;

    @Autowired
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    /**
     * Загрузка картиник поста в БД
     * @param postId идентификатор поста
     * @param file файл с картинкой для записи
     * @return строка с ошибкой, если произошла
     */
    @Async
    @PutMapping(path = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CompletableFuture<ResponseEntity<String>> uploadImage(@PathVariable("id") int postId,
                                                                @RequestParam("image") MultipartFile file) {
        if (file.isEmpty()) {
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body("empty file"));
        }

        var originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body("empty file name"));
        }

        var fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        if (originalFilename.lastIndexOf(".") == -1 || fileExtension.isEmpty())
        {
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body("file name has not extension"));
        }

        return imageService.saveImage(file, postId)
                .thenApplyAsync(r -> ResponseEntity.status(HttpStatus.OK).body("ok"))
                .exceptionally(e -> ResponseEntity.badRequest().body("upload failed: " + e.getMessage()));
    }

    /**
     * Получение картинки поста
     * @param postId идентификатор поста
     * @return тело картинки
     */
    @Async
    @GetMapping(path = "/{id}/image")
    public CompletableFuture<ResponseEntity<Resource>> getImage(@PathVariable("id") int postId) {
        return imageService.getImage(postId).thenApplyAsync(resource -> {
                    if (resource == null) {
                        return ResponseEntity.badRequest().<Resource>build();
                    }

                    return ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getRight() + "\"")
                            .body(resource.getLeft());
                })
                .exceptionallyAsync(ex -> ResponseEntity.badRequest().build());
    }
}
