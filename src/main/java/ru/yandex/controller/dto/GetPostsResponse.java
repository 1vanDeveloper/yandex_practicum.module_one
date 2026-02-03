package ru.yandex.controller.dto;

import java.util.List;

public record GetPostsResponse(List<GetPostResponse> posts) { }
