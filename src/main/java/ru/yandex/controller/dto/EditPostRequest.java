package ru.yandex.controller.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record EditPostRequest(
        @Min(1)
        int id,
        @NotBlank
        String title,
        @NotBlank
        String text,
        @NotNull
        List<String> tags
) { }
