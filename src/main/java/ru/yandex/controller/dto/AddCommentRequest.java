package ru.yandex.controller.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AddCommentRequest(
        @NotBlank
        String text,
        @Min(1)
        int postId
) { }
