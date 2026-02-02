package ru.yandex.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.model.Post;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Управление постами
 */
public interface PostRepository {
    List<Post> getPosts(
            String search,
            int pageNumber,
            int pageSize
    );
}

@Repository
class JdbcNativePostRepository implements PostRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcNativePostRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Post> getPosts(String search,
                               int pageNumber,
                               int pageSize) {
        var valuableWords = Arrays.stream(search.split(" ")).filter(s -> !s.isEmpty() && !s.equals("#")).map(String::strip).toList();
        var tags = valuableWords.stream().filter(s -> s.startsWith("#")).toList();
        var title = valuableWords.stream().filter(s -> !s.startsWith("#")).collect(Collectors.joining(" "));

        var baseSql = "select p.id, p.title, p.text, p.likes_count, count(c.post_id) as comment_count, STRING_AGG(t.name, ', ') AS tags_list " +
                "from posts p " +
                "join comments c on c.post_id = p.id " +
                "join posts_tags pt on p.id = pt.post_id " +
                "join tag t on t.id = pt.tag_id";
        if ((long) tags.size() > 0 || !title.isEmpty())
        {
            baseSql += " where ";
        }
        if (!title.isEmpty())
        {
            baseSql += "p.title like '%" + title + "%'";
            if ((long) tags.size() > 0)
            {
                baseSql += " and ";
            }
        }
        if ((long) tags.size() > 0)
        {
            baseSql += "t.name in ('" + String.join("', '", tags) + "') ";
        }
        baseSql += "group by p.id, p.title, p.text, p.likes_count";
        if ((long) tags.size() > 0)
        {
            baseSql += " having count(distinct t.name) = " + tags.size();
        }

        return jdbcTemplate.query(
                baseSql,
                (rs, rowNum) -> {
                    var post = new Post
                    (
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("text"),
                            rs.getInt("likes_count"),
                            rs.getInt("comment_count")
                    );
                    post.setTags(Arrays.stream(rs.getString("tags_list").split(", ")).toList());
                    return post;
                });
    }

}
