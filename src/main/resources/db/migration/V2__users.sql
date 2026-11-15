CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    login         VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

ALTER TABLE exercise ADD COLUMN user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE exercise DROP CONSTRAINT exercise_name_key;
ALTER TABLE exercise ADD CONSTRAINT exercise_user_name UNIQUE (user_id, name);

CREATE TABLE profile (
    user_id    BIGINT PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    name       VARCHAR(100)     NOT NULL DEFAULT '',
    sex        VARCHAR(20)      NOT NULL,
    age        INTEGER          NOT NULL,
    height     INTEGER          NOT NULL,
    target     DOUBLE PRECISION NOT NULL DEFAULT 0,
    goal       VARCHAR(30)      NOT NULL,
    experience VARCHAR(30)      NOT NULL,
    per_week   INTEGER          NOT NULL,
    priorities VARCHAR(300)     NOT NULL DEFAULT ''
);

CREATE TABLE body_weight (
    id      BIGSERIAL PRIMARY KEY,
    user_id BIGINT           NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    date    DATE             NOT NULL,
    weight  DOUBLE PRECISION NOT NULL,
    CONSTRAINT body_weight_user_date UNIQUE (user_id, date)
);

CREATE TABLE food (
    id      BIGSERIAL PRIMARY KEY,
    user_id BIGINT           NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    date    DATE             NOT NULL,
    name    VARCHAR(200)     NOT NULL,
    grams   DOUBLE PRECISION NOT NULL,
    kcal    DOUBLE PRECISION NOT NULL,
    protein DOUBLE PRECISION NOT NULL,
    fat     DOUBLE PRECISION NOT NULL,
    carbs   DOUBLE PRECISION NOT NULL,
    code    VARCHAR(50)
);

CREATE INDEX food_user_date ON food (user_id, date);
