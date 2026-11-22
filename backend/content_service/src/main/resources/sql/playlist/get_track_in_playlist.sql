SELECT
    pt.position          AS playlist_position,
    t.id                 AS id,
    t.title              AS title,
    t.artist_id          AS artist_id,
    ar.name              AS artist_name,
    t.album_id           AS album_id,
    al.title             AS album_title,
    t.track_number       AS track_number,
    g.name               AS genre,
    COALESCE(t.cover_key, al.cover_key) AS cover_key,
    t.duration_seconds   AS duration_seconds,
    t.explicit_content   AS explicit_content,
    t.play_count         AS play_count,
    t.like_count         AS like_count,
    t.status             AS status,
    t.audio_status       AS audio_status,
    t.published_at       AS published_at
FROM playlist_tracks pt
         JOIN tracks t   ON t.id = pt.track_id
         JOIN artists ar ON ar.id = t.artist_id
         LEFT JOIN albums al ON al.id = t.album_id
         LEFT JOIN genres g  ON g.id = t.genre_id
WHERE pt.playlist_id = :playlistId
  AND pt.position > :cursor
  AND (t.status = 'PUBLISHED' OR ar.owner_user_id = :viewerId)
ORDER BY pt.position ASC
    LIMIT :limit