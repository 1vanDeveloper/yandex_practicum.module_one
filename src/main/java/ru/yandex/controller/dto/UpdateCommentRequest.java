package ru.yandex.controller.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record UpdateCommentRequest(
        @Min(1)
        long id,
        @NotBlank
        String text,
        @Min(1)
        int postId
) { }
