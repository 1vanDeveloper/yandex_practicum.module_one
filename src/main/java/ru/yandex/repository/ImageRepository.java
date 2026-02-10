package ru.yandex.repository;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Types;
import java.util.concurrent.CompletableFuture;

/**
 * Управление картинками
 */
public interface ImageRepository {
    @Async
    CompletableFuture<Integer> saveImage(MultipartFile image, int postId);

    @Async
    CompletableFuture<Pair<Resource, String>> getImage(int postId);
}

class JdbcNativeImageRepository implements ImageRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;

    public JdbcNativeImageRepository(NamedParameterJdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public CompletableFuture<Integer> saveImage(MultipartFile image, int postId) {
        return CompletableFuture.supplyAsync(() -> innerSaveImage(image, postId));
    }

    @Override
    public CompletableFuture<Pair<Resource, String>> getImage(int postId) {
        return CompletableFuture.supplyAsync(() -> innerGetImage(postId));
    }

    private Integer innerSaveImage(MultipartFile image, int postId) {
        var baseSql = """
insert into images (post_id, file_name, content)
values (:post_id, :file_name, :content)
ON CONFLICT (post_id)\s
DO UPDATE SET\s
    file_name = EXCLUDED.file_name,
    content = EXCLUDED.content
""";
        MapSqlParameterSource parameters;
        try {
            parameters = new MapSqlParameterSource()
                .addValue("post_id", postId)
                .addValue("file_name", image.getOriginalFilename())
                .addValue("content", image.getBytes());

            return transactionTemplate.execute(status -> jdbcTemplate.update(baseSql, parameters));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Pair<Resource, String> innerGetImage(int postId) {
        var baseSql = """
select file_name, content from images where post_id = :post_id
""";
        MapSqlParameterSource parameters;
        try {
            parameters = new MapSqlParameterSource()
                .addValue("post_id", postId);

            return transactionTemplate.execute(status ->
                    jdbcTemplate.query(baseSql, parameters, rs -> {
                        if (rs.next()) {
                            return Pair.of(
                                    new InputStreamResource(rs.getBinaryStream("content")),
                                    rs.getString("file_name")
                            );
                        }
                        return null;
                    }));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}


