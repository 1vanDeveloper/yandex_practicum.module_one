package ru.yandex.repository;

import org.springframework.jdbc.core.JdbcTemplate;
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

        var baseSql = "select r.id, r.title, r.text, r.likes_count, r.tags_list, count(distinct c.id) as comment_count from (select p.id, p.title, p.text, p.likes_count, string_agg(distinct t.name, '||') as tags_list " +
                "from posts p " +
                "join posts_tags pt on p.id = pt.post_id " +
                "join tags t on t.id = pt.tag_id";
        if ((long) tags.size() > 0 || !title.isEmpty())
        {
            baseSql += " where ";
        }
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
            baseSql += "t.name in ('" + String.join("', '", tags) + "') ";
        }
        baseSql += "group by p.id, p.title, p.text, p.likes_count";
        if ((long) tags.size() > 0)
        {
            baseSql += " having count(distinct t.name) = " + tags.size();
        }

        baseSql += " offset " + (pageNumber - 1) * pageSize + " limit " + pageSize + ") r " +
                "join comments c on c.post_id = r.id " +
                "group by r.id, r.title, r.text, r.likes_count, r.tags_list";

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
                    post.setTags(Arrays.stream(rs.getString("tags_list").split("\\|\\|")).toList());
                    return post;
                });
    }

}
