package ru.yandex.controller.dto;

import java.util.List;

public record AddPostRequest(String title, String text, List<String> tags) { }
