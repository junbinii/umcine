USE umc_movie;
SHOW TABLES;
SELECT member_id, email, nickname, created_at, updated_at FROM member;
SELECT rating_id, member_id, movie_id, score, comment, created_at, updated_at
FROM rating ORDER BY rating_id DESC;
SHOW CREATE TABLE favorite;
