package ru.yandex.controller.dto;

import java.util.List;

public record PostResponse(int id, String title, String text, List<String> tags, int likesCount, int commentsCount) { }