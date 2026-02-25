package ru.yandex.repository;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import ru.yandex.model.Comment;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Управление комментариями
 */
public interface CommentRepository {
    @Async
    CompletableFuture<List<Comment>> getComments(int postId);

    @Async
    CompletableFuture<Comment> getComment(long commentId);

    @Async
    CompletableFuture<Comment> addComment(int postId, String text);

    @Async
    CompletableFuture<Comment> updateComment(long commentId, String text, int postId);

    @Async
    CompletableFuture<Void> deleteComment(long commentId);
}

class JdbcNativeCommentRepository implements CommentRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcNativeCommentRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public CompletableFuture<List<Comment>> getComments(int postId) {
        return CompletableFuture.supplyAsync(() -> innerGetComments(postId));
    }

    @Override
    public CompletableFuture<Comment> getComment(long commentId) {
        return CompletableFuture.supplyAsync(() -> innerGetComment(commentId));
    }

    @Override
    public CompletableFuture<Comment> addComment(int postId, String text) {
        return CompletableFuture.supplyAsync(() -> innerAddComment(postId, text));
    }

    @Override
    public CompletableFuture<Comment> updateComment(long commentId, String text, int postId) {
        return CompletableFuture.supplyAsync(() -> innerUpdateComment(commentId, text, postId));
    }

    @Override
    public CompletableFuture<Void> deleteComment(long commentId) {
        return CompletableFuture.supplyAsync(() -> innerDeleteComment(commentId));
    }

    private List<Comment> innerGetComments(int postId) {
        var baseSql = """
select id, text, post_id from comments where post_id = :post_id
""";
        var parameters = new MapSqlParameterSource()
                .addValue("post_id", postId);

        return jdbcTemplate.query(baseSql, parameters, (rs, row) -> map(rs));
    }

    private Comment innerGetComment(long commentId) {
        var baseSql = """
select id, text, post_id from comments where id = :id limit 1
""";
        var parameters = new MapSqlParameterSource()
                .addValue("id", commentId);

        return jdbcTemplate.query(baseSql, parameters, JdbcNativeCommentRepository::extractComment);
    }

    private Comment innerAddComment(int postId, String text) {
        var baseSql = """
insert into comments (text, post_id)
values (:text, :post_id)
returning id, text, post_id
""";
        var parameters = new MapSqlParameterSource()
                .addValue("text", text)
                .addValue("post_id", postId);

        return jdbcTemplate.query(baseSql, parameters, JdbcNativeCommentRepository::extractComment);
    }

    private Comment innerUpdateComment(long commentId, String text, int postId) {
        var baseSql = """
update comments
set
    text = :text,
    post_id = :post_id
where id = :id
returning id, text, post_id
""";
        var parameters = new MapSqlParameterSource()
                .addValue("text", text)
                .addValue("post_id", postId)
                .addValue("id", commentId);

        return jdbcTemplate.query(baseSql, parameters, JdbcNativeCommentRepository::extractComment);
    }

    private Void innerDeleteComment(long commentId) {
        var baseSql = """
delete from comments
where id = :id;
""";
        var parameters = new MapSqlParameterSource()
                .addValue("id", commentId);

        jdbcTemplate.update(baseSql, parameters);
        return null;
    }

    private static Comment extractComment(ResultSet rs) throws SQLException, DataAccessException {
        if (rs.next()) {
            return map(rs);
        } else {
            return null;
        }
    }

    private static Comment map(ResultSet rs) throws SQLException {
        return new Comment (
                rs.getInt("id"),
                rs.getString("text"),
                rs.getInt("post_id"));
    }
}
