package ru.yandex.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.repository.ImageRepository;

@RestController
@RequestMapping("/api/posts")
public class ImageController {

    private final ImageRepository imageRepository;

    @Autowired
    public ImageController(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    /**
     * Загрузка картиник поста в БД
     * @param postId идентификатор поста
     * @param file файл с картинкой для записи
     * @return строка с ошибкой, если произошла
     */
    @PutMapping(path = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadImage(@PathVariable("id") int postId,
                                 @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("empty file");
        }

        try {
            var originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                return ResponseEntity.badRequest().body("empty file name");
            }

            var fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            if (originalFilename.lastIndexOf(".") == -1 || fileExtension.isEmpty())
            {
                return ResponseEntity.badRequest().body("file name has not extension");
            }

            imageRepository.saveImage(file, postId).get();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("upload failed: " + e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body("ok");
    }

    /**
     * Получение картинки поста
     * @param postId идентификатор поста
     * @return тело картинки
     */
    @GetMapping(path = "/{id}/image")
    public ResponseEntity<Resource> getImage(@PathVariable("id") int postId) {
        try {
            var resource = imageRepository.getImage(postId).get();

            if (resource == null) {
                return ResponseEntity.badRequest().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getRight() + "\"")
                    .body(resource.getLeft());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
