CREATE TABLE cafe_crowd_report (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cafe_id       BIGINT      NOT NULL REFERENCES cafe (id) ON DELETE CASCADE,
    crowd_level   VARCHAR(16) NOT NULL CHECK (crowd_level IN ('QUIET', 'NORMAL', 'CROWDED')),
    source        VARCHAR(32) NOT NULL DEFAULT 'USER_REPORT',
    reporter_hash VARCHAR(64) NOT NULL,
    observed_at   TIMESTAMPTZ NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_report_cafe_observed ON cafe_crowd_report (cafe_id, observed_at DESC);
CREATE INDEX idx_report_reporter_observed ON cafe_crowd_report (reporter_hash, observed_at DESC);
