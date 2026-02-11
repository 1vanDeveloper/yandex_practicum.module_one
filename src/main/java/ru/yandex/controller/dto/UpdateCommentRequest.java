package ru.yandex.controller.dto;

public record UpdateCommentRequest(long id, String text, int postId) { }
