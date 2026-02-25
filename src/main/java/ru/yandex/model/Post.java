package ru.yandex.model;

import java.util.List;

/**
 * Пост
 */
public class Post {

    private final int id;
    private final String title;
    private final String text;
    private final int likesCount;
    private final int commentsCount;
    private Comment[] comments;
    private List<String> tags;

    public Post(int id, String title, String text, int likesCount, int commentsCount) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.likesCount = likesCount;
        this.commentsCount = commentsCount;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public Comment[] getComments() {
        return comments;
    }

    public void setComments(Comment[] comments) {
        this.comments = comments;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public int getCommentsCount() {
        return commentsCount;
    }
}
