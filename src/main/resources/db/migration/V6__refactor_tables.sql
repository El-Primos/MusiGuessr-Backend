-- ============================================================
-- Musics Table
-- ============================================================
ALTER TABLE musics
    ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT NOW();

ALTER TABLE musics
    ADD COLUMN owner_id BIGINT REFERENCES users(id) ON DELETE SET NULL;


-- ============================================================
-- Playlists Table
-- ============================================================
ALTER TABLE playlists
    RENAME COLUMN user_id to owner_id;


-- ============================================================
-- Tournaments Table
-- ============================================================
ALTER TABLE tournaments
    RENAME COLUMN create_date TO created_at;

ALTER TABLE tournaments
    RENAME COLUMN status TO state;

ALTER TABLE tournaments
    RENAME COLUMN creator_id TO owner_id;

ALTER TABLE tournaments
    ALTER COLUMN owner_id DROP NOT NULL;

ALTER TABLE tournaments
    ALTER COLUMN start_date SET NOT NULL;

ALTER TABLE tournaments
    ALTER COLUMN end_date SET NOT NULL;


-- ============================================================
-- Games Table
-- ============================================================
ALTER TABLE games
    DROP COLUMN is_offline,
    DROP COLUMN type;

ALTER TABLE games
    DROP COLUMN played_at;

ALTER TABLE games
    ADD COLUMN state VARCHAR(20) NOT NULL DEFAULT 'CREATED';

ALTER TABLE games
    ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT NOW();

ALTER TABLE games
    RENAME COLUMN creator_id to owner_id;

ALTER TABLE games
    ALTER COLUMN owner_id DROP NOT NULL;

ALTER TABLE games
    DROP CONSTRAINT games_creator_id_fkey;

ALTER TABLE games
    ADD CONSTRAINT games_owner_id_fkey
        FOREIGN KEY (owner_id)
            REFERENCES users(id)
            ON DELETE CASCADE;

-- ============================================================
-- GamesHistory Table
-- ============================================================
ALTER TABLE game_history
    RENAME COLUMN user_score TO score;

ALTER TABLE game_history
    ADD COLUMN id BIGSERIAL;

ALTER TABLE game_history
    DROP CONSTRAINT game_history_pkey;

ALTER TABLE game_history
    ADD CONSTRAINT game_history_pkey PRIMARY KEY (id);


-- ============================================================
-- GamesRounds Table
-- ============================================================
CREATE TABLE game_rounds (
  id                    BIGSERIAL PRIMARY KEY,
  game_history_id       BIGINT NOT NULL REFERENCES game_history(id) ON DELETE CASCADE,
  song                  TEXT NOT NULL,
  guessed_song          TEXT,
  guess_time            INTEGER NOT NULL,
  score_earned          INTEGER NOT NULL,
  round                 INTEGER NOT NULL
);

-- ============================================================
-- Posts Table
-- ============================================================
ALTER TABLE posts
    DROP COLUMN game_id,
    DROP COLUMN content,
    DROP COLUMN image;

ALTER TABLE posts
    ADD COLUMN game_history_id BIGINT NOT NULL REFERENCES game_history(id) ON DELETE CASCADE ;