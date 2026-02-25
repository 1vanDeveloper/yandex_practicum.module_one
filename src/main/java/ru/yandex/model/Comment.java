package ru.yandex.model;

/**
 * Комментарий к посту
 */
public class Comment {

    private long id;
    private String text;
    private int postId;

    public Comment(long id, String text, int postId) {
        this.id = id;
        this.text = text;
        this.postId = postId;
    }

    public long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public int getPostId() {
        return postId;
    }
}
