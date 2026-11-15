CREATE TABLE exercise (
    id       BIGSERIAL PRIMARY KEY,
    name     VARCHAR(100) NOT NULL UNIQUE,
    favorite INTEGER      NOT NULL DEFAULT 0
);

CREATE TABLE entry (
    id          BIGSERIAL PRIMARY KEY,
    exercise_id BIGINT           NOT NULL REFERENCES exercise (id) ON DELETE CASCADE,
    date        DATE             NOT NULL,
    weight      DOUBLE PRECISION NOT NULL,
    reps        INTEGER          NOT NULL
);

CREATE INDEX entry_exercise_date ON entry (exercise_id, date);
