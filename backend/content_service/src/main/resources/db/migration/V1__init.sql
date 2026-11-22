-- content-service schema, stage 1 (MariaDB). Owned by Flyway: add V3__..., never edit a deployed file.

CREATE TABLE genres (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(80)  NOT NULL,
    slug       VARCHAR(80)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_genre_name (name),
    UNIQUE KEY uk_genre_slug (slug)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

INSERT INTO genres (name, slug) VALUES
    ('Pop', 'pop'), ('Rock', 'rock'), ('Hip-Hop', 'hip-hop'), ('Electronic', 'electronic'),
    ('R&B', 'r-and-b'), ('Jazz', 'jazz'), ('Classical', 'classical'), ('Indie', 'indie'), ('V-Pop', 'v-pop');

-- an artist = a user with role ARTIST (owner_user_id = user id of auth-service); denormalized name for listings
CREATE TABLE artists (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    owner_user_id BIGINT        NOT NULL,
    name          VARCHAR(150)  NOT NULL,
    bio           VARCHAR(1000) NULL,
    image_key     VARCHAR(255)  NULL,            -- object key in the cover bucket
    created_at    DATETIME(6)   NOT NULL,
    updated_at    DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_artist_owner (owner_user_id),
    KEY idx_artist_name (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE albums (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    artist_id    BIGINT       NOT NULL,
    title        VARCHAR(200) NOT NULL,
    cover_key    VARCHAR(255) NULL,
    release_date DATE         NULL,
    status       VARCHAR(16)  NOT NULL DEFAULT 'DRAFT',     -- DRAFT | PUBLISHED | HIDDEN | TAKEN_DOWN
    published_at DATETIME(6)  NULL,
    created_at   DATETIME(6)  NOT NULL,
    updated_at   DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_album_artist (artist_id, id),
    KEY idx_album_status (status, id),
    KEY idx_album_title (title)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE tracks (
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    artist_id          BIGINT       NOT NULL,
    album_id           BIGINT       NULL,
    track_number       INT          NULL,
    title              VARCHAR(200) NOT NULL,
    genre_id           BIGINT       NULL,
    cover_key          VARCHAR(255) NULL,                    -- falls back to the album cover
    audio_key          VARCHAR(255) NULL,                    -- object key in the PRIVATE audio bucket
    audio_content_type VARCHAR(64)  NULL,
    audio_size_bytes   BIGINT       NULL,
    duration_seconds   INT          NULL,
    bitrate_kbps       INT          NULL,
    audio_status       VARCHAR(16)  NOT NULL DEFAULT 'PENDING_UPLOAD',   -- PENDING_UPLOAD | READY
    status             VARCHAR(16)  NOT NULL DEFAULT 'DRAFT',            -- DRAFT | PUBLISHED | HIDDEN | TAKEN_DOWN
    explicit_content   TINYINT(1)   NOT NULL DEFAULT 0,
    play_count         BIGINT       NOT NULL DEFAULT 0,
    like_count         BIGINT       NOT NULL DEFAULT 0,
    published_at       DATETIME(6)  NULL,
    created_at         DATETIME(6)  NOT NULL,
    updated_at         DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_track_status (status, id),                       -- new releases
    KEY idx_track_genre (genre_id, status, id),
    KEY idx_track_artist (artist_id, status, id),
    KEY idx_track_album (album_id, track_number),
    FULLTEXT KEY ft_track_title (title)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE playlists (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    owner_user_id BIGINT       NOT NULL,
    name          VARCHAR(150) NOT NULL,
    description   VARCHAR(500) NULL,
    visibility    VARCHAR(16)  NOT NULL DEFAULT 'PRIVATE',    -- PUBLIC | PRIVATE
    track_count   INT          NOT NULL DEFAULT 0,
    created_at    DATETIME(6)  NOT NULL,
    updated_at    DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_playlist_owner (owner_user_id, id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE playlist_tracks (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    playlist_id BIGINT      NOT NULL,
    track_id    BIGINT      NOT NULL,
    position    INT         NOT NULL,
    added_at    DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_playlist_track (playlist_id, track_id),
    KEY idx_playlist_position (playlist_id, position)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE track_likes (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    user_id    BIGINT      NOT NULL,
    track_id   BIGINT      NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_track_like (user_id, track_id),
    KEY idx_like_user (user_id, id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE saved_albums (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    user_id    BIGINT      NOT NULL,
    album_id   BIGINT      NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_saved_album (user_id, album_id),
    KEY idx_saved_user (user_id, id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE play_history (
    id        BIGINT      NOT NULL AUTO_INCREMENT,
    user_id   BIGINT      NOT NULL,
    track_id  BIGINT      NOT NULL,
    played_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_history_user (user_id, id),
    KEY idx_history_played (played_at)                        -- retention job
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
