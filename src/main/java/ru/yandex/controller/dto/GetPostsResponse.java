package ru.yandex.controller.dto;

import java.util.List;

public record GetPostsResponse(List<PostResponse> posts, boolean hasPrev, boolean hasNext, int lastPage) { }
