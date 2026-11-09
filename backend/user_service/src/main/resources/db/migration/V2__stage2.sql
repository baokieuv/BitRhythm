-- profile_status ACTIVE/HIDDEN/DELETED đã dùng chuỗi ở V1, không cần đổi cột.

-- index tìm kiếm tiền tố (text_pattern_ops cho LIKE 'abc%')
CREATE INDEX idx_profiles_display_name ON user_profiles (lower(display_name) text_pattern_ops);

CREATE TABLE user_blocks (
    id          BIGSERIAL PRIMARY KEY,
    blocker_id  BIGINT    NOT NULL,
    blocked_id  BIGINT    NOT NULL,
    created_at  TIMESTAMP NOT NULL,
    CONSTRAINT uq_block UNIQUE (blocker_id, blocked_id)
);
CREATE INDEX idx_blocks_blocked ON user_blocks (blocked_id);

CREATE TABLE artist_verification_requests (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT       NOT NULL,
    status       VARCHAR(20)  NOT NULL,
    message      VARCHAR(1000),
    reviewed_by  BIGINT,
    review_note  VARCHAR(1000),
    created_at   TIMESTAMP    NOT NULL,
    reviewed_at  TIMESTAMP
);
CREATE INDEX idx_verif_user ON artist_verification_requests (user_id);
CREATE INDEX idx_verif_status ON artist_verification_requests (status, id);

CREATE TABLE processed_events (
    event_id      VARCHAR(200) PRIMARY KEY,
    processed_at  TIMESTAMP NOT NULL
);
