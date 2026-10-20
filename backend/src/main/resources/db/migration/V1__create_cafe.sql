CREATE TABLE cafe (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    external_place_id VARCHAR(64)  NOT NULL UNIQUE,
    name              VARCHAR(200) NOT NULL,
    address           VARCHAR(300),
    latitude          DOUBLE PRECISION NOT NULL CHECK (latitude BETWEEN -90 AND 90),
    longitude         DOUBLE PRECISION NOT NULL CHECK (longitude BETWEEN -180 AND 180),
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_cafe_lat_lng ON cafe (latitude, longitude);
CREATE INDEX idx_cafe_name ON cafe (name);
