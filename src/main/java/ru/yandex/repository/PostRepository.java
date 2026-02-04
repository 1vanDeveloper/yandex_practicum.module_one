package ru.yandex.repository;

import org.slf4j.Logger;
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
    private final Logger logger;

    public JdbcNativePostRepository(JdbcTemplate jdbcTemplate, Logger logger) {
        this.jdbcTemplate = jdbcTemplate;
        this.logger = logger;
    }

    @Override
    public List<Post> getPosts(String search,
                               int pageNumber,
                               int pageSize) {
        var valuableWords = Arrays.stream(search.split(" ")).filter(s -> !s.isEmpty() && !s.equals("#")).map(String::strip).toList();
        var tags = valuableWords.stream().filter(s -> s.startsWith("#")).map(s -> s.substring(1)).toList();
        var title = valuableWords.stream().filter(s -> !s.startsWith("#")).collect(Collectors.joining(" "));

        var baseSql = "select p.id, p.title, p.text, p.likes_count, string_agg(distinct t.name, '||') as tags_list from posts p " +
                "left join posts_tags pt on p.id = pt.post_id " +
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

        baseSql += " offset " + (pageNumber - 1) * pageSize + " limit " + pageSize;

        baseSql = "select r.id, r.title, r.text, r.likes_count, string_agg(distinct t1.name, '||') as tags_list, count(distinct c.id) as comment_count from " +
                    "(" + baseSql + ") r " +
                    "left join comments c on c.post_id = r.id " +
                    "left join posts_tags pt1 on r.id = pt1.post_id " +
                    "join tags t1 on t1.id = pt1.tag_id " +
                    "group by r.id, r.title, r.text, r.likes_count " +
                    "order by r.id";

        logger.debug(baseSql);

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
