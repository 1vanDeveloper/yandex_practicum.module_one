package ru.yandex.repository;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import ru.yandex.model.Post;
import ru.yandex.repository.dto.SearchResult;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Управление постами
 */
public interface PostRepository {
    @Async
    CompletableFuture<SearchResult> getPosts(
            String search,
            int pageNumber,
            int pageSize
    );

    @Async
    CompletableFuture<Post> getPost(
            int id
    );
}

class JdbcNativePostRepository implements PostRepository {

    private final JdbcTemplate jdbcTemplate;
    private final Logger logger;

    public JdbcNativePostRepository(JdbcTemplate jdbcTemplate, Logger logger) {
        this.jdbcTemplate = jdbcTemplate;
        this.logger = logger;
    }

    @Override
    public CompletableFuture<SearchResult> getPosts(String search,
                                                  int pageNumber,
                                                  int pageSize) {
        return CompletableFuture.supplyAsync(() -> innerGetPosts(search, pageNumber, pageSize)).thenApply(r -> {
            var total = r.isEmpty() ? 0 : r.getFirst().getRight();
            var totalPages = total / pageSize + ((total % pageSize > 0) ? 1 : 0);
            var posts = r.stream().map(Pair::getLeft).toList();
            return new SearchResult(posts, pageNumber > 1, pageNumber < totalPages, totalPages);
        });
    }

    @Override
    public CompletableFuture<Post> getPost(int id) {
        return CompletableFuture.supplyAsync(() -> innerGetPost(id));
    }

    private List<Pair<Post, Integer>> innerGetPosts(String search,
                                                    int pageNumber,
                                                    int pageSize) {
        var valuableWords = Arrays.stream(search.split(" ")).filter(s -> !s.isEmpty() && !s.equals("#")).map(String::strip).toList();
        var tags = valuableWords.stream().filter(s -> s.startsWith("#")).map(s -> s.substring(1)).toList();
        var title = valuableWords.stream().filter(s -> !s.startsWith("#")).collect(Collectors.joining(" "));

        var baseSql = "select p.id, p.title, p.text, p.likes_count from posts p ";
        if ((long) tags.size() > 0)
        {
            baseSql += "left join posts_tags pt on p.id = pt.post_id " +
                    "join tags t on t.id = pt.tag_id ";
        }

        if ((long) tags.size() > 0 || !title.isEmpty())
        {
            baseSql += " where ";

            if (!title.isEmpty())
            {
                baseSql += "p.title like '%" + title + "%' ";
                if ((long) tags.size() > 0)
                {
                    baseSql += " and ";
                }
            }
            if ((long) tags.size() > 0)
            {
                baseSql += "t.name in ('" + String.join("', '", tags) + "') " +
                        "group by p.id, p.title, p.text, p.likes_count " +
                        "having count(distinct t.name) = " + tags.size();
            }
        }

        baseSql = sqlPostSelect(baseSql);

        baseSql = "with result as (" + baseSql + "), " +
                "total AS ( " +
                "select count(id) as id_count from result " +
                ") select result.*, total.id_count as total_count from result, total";

        baseSql += " offset " + (pageNumber - 1) * pageSize + " limit " + pageSize;
        logger.debug(baseSql);

        return jdbcTemplate.query(
                baseSql,
                (rs, rowNum) -> Pair.of(map(rs), rs.getInt("total_count")));
    }

    private Post innerGetPost(int id) {
        var baseSql = "select p.id, p.title, p.text, p.likes_count from posts p where p.id = " + id;
        baseSql = sqlPostSelect(baseSql);

        return jdbcTemplate.query(baseSql, JdbcNativePostRepository::extractPost);
    }

    private static Post extractPost(ResultSet rs) throws SQLException, DataAccessException {
        if (rs.next()) {
            return map(rs);
        } else {
            // Handle case where no rows are returned
            return null;
        }
    }

    private static Post map(ResultSet rs) throws SQLException {
        var post = new Post (
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("text"),
                    rs.getInt("likes_count"),
                    rs.getInt("comment_count")
                );
        post.setTags(Arrays.stream(rs.getString("tags_list").split("\\|\\|")).toList());
        return post;
    }

    private static String sqlPostSelect(String setOfPosts) {
        setOfPosts = setOfPosts.trim();
        if (setOfPosts.startsWith("select") || setOfPosts.startsWith("SELECT"))
        {
            setOfPosts = "(" + setOfPosts + ")";
        }

        return "select r.id, r.title, r.text, r.likes_count, string_agg(distinct t1.name, '||') as tags_list, count(distinct c.id) as comment_count from " +
                setOfPosts + " r " +
                "left join comments c on c.post_id = r.id " +
                "left join posts_tags pt1 on r.id = pt1.post_id " +
                "join tags t1 on t1.id = pt1.tag_id " +
                "group by r.id, r.title, r.text, r.likes_count " +
                "order by r.id";
    }
}
