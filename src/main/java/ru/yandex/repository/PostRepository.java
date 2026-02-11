package ru.yandex.repository;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
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

    @Async
    CompletableFuture<Post> addPost(
            String title,
            String text,
            List<String> tags
    );

    @Async
    CompletableFuture<Post> editPost(
            int id,
            String title,
            String text,
            List<String> tags
    );

    @Async
    CompletableFuture<Void> deletePost(
            int id
    );

    @Async
    CompletableFuture<Integer> likeIncrease(
            int id
    );
}

class JdbcNativePostRepository implements PostRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final Logger logger;

    public JdbcNativePostRepository(NamedParameterJdbcTemplate jdbcTemplate, Logger logger) {
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

    @Override
    public CompletableFuture<Post> addPost(String title, String text, List<String> tags) {
        return CompletableFuture.supplyAsync(() -> innerAddPost(title, text, tags));
    }

    @Override
    public CompletableFuture<Post> editPost(int id, String title, String text, List<String> tags) {
        return CompletableFuture.supplyAsync(() -> innerEditPost(id, title, text, tags));
    }

    @Override
    public CompletableFuture<Void> deletePost(int id) {
        return CompletableFuture.supplyAsync(() -> innerDeletePost(id));
    }

    @Override
    public CompletableFuture<Integer> likeIncrease(int id) {
        return CompletableFuture.supplyAsync(() -> innerLikeIncrease(id));
    }

    private Integer innerLikeIncrease(int id) {
        var baseSql = """
update posts
set
    likes_count = likes_count + 1
where
    id = :post_id
returning likes_count
""";

        var parameters = new MapSqlParameterSource()
                .addValue("post_id", id);
        return jdbcTemplate.query(baseSql, parameters, rs -> {
            if (rs.next()) {
                return rs.getInt("likes_count");
            }
            return 0;
        });
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
            baseSql += """
left join posts_tags pt on p.id = pt.post_id
join tags t on t.id = pt.tag_id
""";
        }

        var parameters = new MapSqlParameterSource();

        if ((long) tags.size() > 0 || !title.isEmpty())
        {
            baseSql += " where ";

            if (!title.isEmpty())
            {
                baseSql += "p.title like :title ";
                parameters = parameters.addValue("title", "%" + title + "%");
                if ((long) tags.size() > 0)
                {
                    baseSql += " and ";
                }
            }
            if ((long) tags.size() > 0)
            {
                baseSql += """
t.name in (:tags)
group by p.id, p.title, p.text, p.likes_count
having count(distinct t.name) = :tagsSize
""";
                parameters = parameters
                        .addValue("tags", tags)
                        .addValue("tagsSize", tags.size());
            }
        }

        baseSql = """
with result as (
"""
+ sqlPostSelect(baseSql) +
"""
),
total AS (
    select count(id) as id_count from result
)
select
    result.*,
    total.id_count as total_count
from
    result, total
offset :offset
limit :limit
""";
        parameters = parameters
                .addValue("offset", (pageNumber - 1) * pageSize)
                .addValue("limit", pageSize);
        logger.debug(baseSql);

        return jdbcTemplate.query(
                baseSql,
                parameters,
                (rs, rowNum) -> Pair.of(map(rs), rs.getInt("total_count")));
    }

    private Void innerDeletePost(int id) {
        var baseSql = """
with deleted_comments as (
    delete from comments
    where post_id = :post_id
    returning id
),
deleted_tags as (
    delete from posts_tags
    where post_id = :post_id
    returning tag_id
)
delete from posts
where id = :post_id;
""";
        var parameters = new MapSqlParameterSource()
                .addValue("post_id", id);
        jdbcTemplate.update(baseSql, parameters);
        return null;
    }

    private Post innerEditPost(int id, String title, String text, List<String> tags) {
        upsertTags(tags);
        var baseSql = """
with edit_post as (
    update posts
    set
        title = :title,
        text = :text
    where id = :id
    returning id, title, text, likes_count
)
""";
        if (!tags.isEmpty())
            baseSql += """
,
deleted_tags AS (
    delete from posts_tags
    using tags
    where post_id = :id
        and tag_id = tags.id
        and tags.name not in (:tags)
    returning post_id, tag_id
),
ins_tags AS (
    insert into posts_tags (post_id, tag_id)
    select ep.id, t.id
    from tags t
    cross join edit_post ep
    where t.name in (:tags)
    on conflict (post_id, tag_id) do nothing
    returning post_id, tag_id
),
final_tags as (
    select post_id, tag_id from posts_tags where post_id = :id
    except
    select post_id, tag_id from deleted_tags
    union
    select post_id, tag_id from ins_tags
)
""";
        if (tags.isEmpty()) {
            baseSql += sqlPostSelect("edit_post");
        } else {
            baseSql += sqlPostSelect("edit_post").replace("posts_tags", "final_tags");
        }

        var parameters = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("title", title)
                .addValue("text", text)
                .addValue("tags", tags);
        return jdbcTemplate.query(baseSql, parameters, JdbcNativePostRepository::extractPost);
    }

    private Post innerAddPost(String title, String text, List<String> tags) {
        upsertTags(tags);
        var baseSql = """
with new_post as (
    insert into posts (title, text, likes_count)
    values (:title, :text, 0)
    returning id, title, text, likes_count
),
ins_tags as (
    insert into posts_tags (post_id, tag_id)
    select new_post.id, t.id
    from tags t, new_post
    where t.name in (:tags)
    returning post_id, (select id from tags where id = tag_id) as tag_id
)
select
    p.id,
    p.title,
    p.text,
    p.likes_count,
    string_agg(distinct t.name, '||') as tags_list,
    0 as comment_count
from
    new_post p
left join ins_tags pt on p.id = pt.post_id
join tags t on t.id = pt.tag_id
group by p.id, p.title, p.text, p.likes_count
order by p.id
""";

        var parameters = new MapSqlParameterSource()
                .addValue("title", title)
                .addValue("text", text)
                .addValue("tags", tags);
        return jdbcTemplate.query(baseSql, parameters, JdbcNativePostRepository::extractPost);
    }

    private void upsertTags(List<String> tags) {
        if (tags.isEmpty()) {
            return;
        }

        var sql = """
insert into tags (name)
values (:name)
on conflict (name) do nothing
""";
        SqlParameterSource[] batchParams = tags.stream()
                .map(item -> new MapSqlParameterSource()
                        .addValue("name", item))
                .toArray(SqlParameterSource[]::new);
        jdbcTemplate.batchUpdate(sql, batchParams);
    }

    private Post innerGetPost(int id) {
        var baseSql = "select p.id, p.title, p.text, p.likes_count from posts p where p.id = :id";
        baseSql = sqlPostSelect(baseSql);
        var parameters = new MapSqlParameterSource().addValue("id", id);

        return jdbcTemplate.query(baseSql, parameters, JdbcNativePostRepository::extractPost);
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

        return """
select
    r.id,
    r.title,
    r.text,
    r.likes_count,
    string_agg(distinct t1.name, '||') as tags_list,
    count(distinct c.id) as comment_count
from\s""" + setOfPosts + """
 r
left join comments c on c.post_id = r.id
left join posts_tags pt1 on r.id = pt1.post_id
left join tags t1 on t1.id = pt1.tag_id
group by r.id, r.title, r.text, r.likes_count
order by r.id
""";
    }
}
