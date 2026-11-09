CREATE TABLE user_profiles (
    user_id          BIGINT PRIMARY KEY,
    display_name     VARCHAR(100) NOT NULL,
    bio              VARCHAR(500),
    country          VARCHAR(2),
    avatar_key       VARCHAR(255),
    verified         BOOLEAN      NOT NULL DEFAULT FALSE,
    status           VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    follower_count   BIGINT       NOT NULL DEFAULT 0,
    following_count  BIGINT       NOT NULL DEFAULT 0,
    created_at       TIMESTAMP    NOT NULL,
    updated_at       TIMESTAMP    NOT NULL
);

CREATE TABLE user_settings (
    user_id                  BIGINT PRIMARY KEY,
    locale                   VARCHAR(5)  NOT NULL DEFAULT 'vi',
    profile_visibility       VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    show_listening_activity  BOOLEAN     NOT NULL DEFAULT TRUE
);

CREATE TABLE follows (
    id           BIGSERIAL PRIMARY KEY,
    follower_id  BIGINT    NOT NULL,
    followee_id  BIGINT    NOT NULL,
    created_at   TIMESTAMP NOT NULL,
    CONSTRAINT uq_follow UNIQUE (follower_id, followee_id)
);
CREATE INDEX idx_follows_followee ON follows (followee_id, id);
CREATE INDEX idx_follows_follower ON follows (follower_id, id);

CREATE TABLE outbox (
    id            BIGSERIAL PRIMARY KEY,
    aggregate_id  VARCHAR(100) NOT NULL,
    event_id      VARCHAR(200) NOT NULL,
    event_type    VARCHAR(100) NOT NULL,
    topic         VARCHAR(200) NOT NULL,
    payload       TEXT         NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at    TIMESTAMP    NOT NULL,
    sent_at       TIMESTAMP
);
CREATE INDEX idx_outbox_status ON outbox (status, id);
