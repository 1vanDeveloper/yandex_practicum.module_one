package ru.yandex.repository.dto;

import ru.yandex.model.Post;

import java.util.List;

public record SearchResult(List<Post> posts, boolean hasPrev, boolean hasNext, int lastPage) { }
