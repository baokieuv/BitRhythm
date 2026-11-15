-- content-service schema, stage 2.

CREATE TABLE content_reports (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    reporter_user_id BIGINT        NOT NULL,
    target_type      VARCHAR(16)   NOT NULL,        -- TRACK | ALBUM | PLAYLIST
    target_id        BIGINT        NOT NULL,
    reason           VARCHAR(16)   NOT NULL,        -- COPYRIGHT | ABUSE | OTHER
    details          VARCHAR(1000) NULL,
    status           VARCHAR(16)   NOT NULL,        -- OPEN | RESOLVED | DISMISSED
    resolved_by      BIGINT        NULL,
    resolution_note  VARCHAR(500)  NULL,
    created_at       DATETIME(6)   NOT NULL,
    resolved_at      DATETIME(6)   NULL,
    PRIMARY KEY (id),
    KEY idx_report_status (status, id),
    KEY idx_report_target (target_type, target_id),
    KEY idx_report_reporter (reporter_user_id, target_type, target_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE track_lyrics (
    track_id   BIGINT      NOT NULL,
    plain_text LONGTEXT    NULL,
    synced_lrc LONGTEXT    NULL,                    -- LRC format: [mm:ss.xx] line
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (track_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE track_credits (
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    track_id BIGINT       NOT NULL,
    role     VARCHAR(40)  NOT NULL,                 -- WRITER | PRODUCER | COMPOSER | ...
    name     VARCHAR(150) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_credit_track (track_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- same layout as auth-service's outbox
CREATE TABLE outbox (
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    aggregate_type VARCHAR(64) NOT NULL,
    aggregate_id   VARCHAR(64) NOT NULL,
    event_type     VARCHAR(64) NOT NULL,
    payload        LONGTEXT    NOT NULL,
    is_processed   INT         NOT NULL DEFAULT 0,
    created_at     DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_outbox_pending (is_processed, id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- events of auth-service already handled (idempotent consumer)
CREATE TABLE processed_events (
    event_id     VARCHAR(64) NOT NULL,
    processed_at DATETIME(6) NOT NULL,
    PRIMARY KEY (event_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
