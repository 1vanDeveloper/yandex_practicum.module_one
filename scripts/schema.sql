CREATE DATABASE blog;
\c blog

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE IF NOT EXISTS posts(
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title varchar(256),
    text TEXT,
    likes_count INT
);
CREATE INDEX IF NOT EXISTS posts_title_trgm_idx ON posts USING GIN (to_tsvector('russian'::regconfig, title));

CREATE TABLE IF NOT EXISTS comments(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    text TEXT,
    post_id INT REFERENCES posts(id)
);
CREATE INDEX IF NOT EXISTS comments_post_idx ON comments(post_id);

CREATE TABLE IF NOT EXISTS tags(
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name varchar(256) UNIQUE
);

CREATE TABLE IF NOT EXISTS posts_tags(
    post_id INT REFERENCES posts(id),
    tag_id INT REFERENCES tags(id),
    PRIMARY KEY (post_id, tag_id)
);