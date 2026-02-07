package ru.yandex.controller.dto;

import java.util.List;

public record EditPostRequest(int id, String title, String text, List<String> tags) { }
