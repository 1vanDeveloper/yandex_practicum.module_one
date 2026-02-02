\c blog

INSERT INTO posts (title, text, likes_count)
VALUES
    ('Title 1 first', 'Text first text text 1', 9),
    ('Title 2 second', 'Text second text text 2', 10),
    ('Title 3 third', 'Text third text text 3', 11),
    ('Title 4 forth', 'Text forth text text 4', 1);

INSERT INTO comments (text, post_id)
VALUES
    ('Comment 1 for post 1', 1),
    ('Comment 2 for post 1', 1),
    ('Comment 1 for post 2', 2),
    ('Comment 2 for post 2', 2),
    ('Comment for post 3', 3);

INSERT INTO tags (name)
VALUES
    ('Tag for 1'),
    ('Tag for 2'),
    ('Tag for 3'),
    ('Tag for 4'),
    ('Tag for 1, 3'),
    ('Tag for all');

INSERT INTO posts_tags (post_id, tag_id)
VALUES
    (1, 1),
    (2, 2),
    (3, 3),
    (4, 4),
    (1, 5),
    (3, 5),
    (1, 6),
    (2, 6),
    (3, 6),
    (4, 6);