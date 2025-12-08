-- =============== USERS & ADMINS ===============
CREATE TABLE users (
  id           BIGSERIAL PRIMARY KEY,
  name         TEXT NOT NULL,
  user_name    TEXT NOT NULL UNIQUE,
  email        TEXT NOT NULL UNIQUE,
  password     TEXT NOT NULL,
  score        INTEGER NOT NULL DEFAULT 0,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE admins (
  id           BIGSERIAL PRIMARY KEY,
  name         TEXT NOT NULL,
  user_name    TEXT NOT NULL UNIQUE,
  email        TEXT NOT NULL UNIQUE,
  password     TEXT NOT NULL,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =============== FOLLOWINGS ===============
CREATE TABLE followings (
  user_id        BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  following_id   BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  PRIMARY KEY (user_id, following_id),
  CONSTRAINT no_self_follow CHECK (user_id <> following_id)
);

-- =============== MUSIC CATALOG ===============
CREATE TABLE artists (
  id    BIGSERIAL PRIMARY KEY,
  name  TEXT NOT NULL
);

CREATE TABLE genre (
  id     BIGSERIAL PRIMARY KEY,
  genre  TEXT NOT NULL UNIQUE
);

CREATE TABLE albums (
  id         BIGSERIAL PRIMARY KEY,
  name       TEXT NOT NULL,
  artist_id  BIGINT NOT NULL REFERENCES artists(id) ON DELETE RESTRICT
);

CREATE TABLE musics (
  id            BIGSERIAL PRIMARY KEY,
  name          TEXT NOT NULL,
  genre_id      BIGINT REFERENCES genre(id) ON DELETE SET NULL,
  album_id      BIGINT REFERENCES albums(id) ON DELETE SET NULL,
  url           TEXT NOT NULL,
  release_date  DATE,
  language      TEXT
);

-- =============== PLAYLISTS ===============
CREATE TABLE playlists (
  id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name       TEXT NOT NULL,
  user_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT uq_playlists_owner_name UNIQUE (user_id, name)
);

CREATE TABLE playlist_items (
  playlist_id BIGINT NOT NULL REFERENCES playlists(id) ON DELETE CASCADE,
  position    INTEGER NOT NULL CHECK (position > 0),
  music_id    BIGINT NOT NULL REFERENCES musics(id) ON DELETE CASCADE,
  PRIMARY KEY (playlist_id, position),
  UNIQUE (playlist_id, music_id)
);

-- =============== TOURNAMENTS ===============
CREATE TABLE tournaments (
  id           BIGSERIAL PRIMARY KEY,
  creator_id   BIGINT NOT NULL REFERENCES admins(id) ON DELETE RESTRICT,
  playlist_id  BIGINT REFERENCES playlists(id) ON DELETE SET NULL,
  name         TEXT NOT NULL,
  description  TEXT,
  create_date  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  start_date   TIMESTAMPTZ,   -- was start_data
  end_date     TIMESTAMPTZ,
  CONSTRAINT tournament_time_ok CHECK (
    start_date IS NULL OR end_date IS NULL OR start_date <= end_date
  )
);

CREATE TABLE tournament_info (
  tournament_id BIGINT NOT NULL REFERENCES tournaments(id) ON DELETE CASCADE,
  user_id       BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  user_score    INTEGER NOT NULL DEFAULT 0,
  PRIMARY KEY (tournament_id, user_id)
);

-- =============== GAMES & HISTORY ===============
CREATE TABLE games (
  id          BIGSERIAL PRIMARY KEY,
  creator_id  BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  is_offline  BOOLEAN NOT NULL DEFAULT FALSE,
  type        TEXT,
  played_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  playlist_id BIGINT REFERENCES playlists(id) ON DELETE SET NULL   -- was "playlists"
);

CREATE TABLE game_history (
  game_id     BIGINT NOT NULL REFERENCES games(id) ON DELETE CASCADE,
  user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  user_score  INTEGER NOT NULL DEFAULT 0,
  PRIMARY KEY (game_id, user_id)
);

-- =============== POSTS ===============
CREATE TABLE posts (
  id          BIGSERIAL PRIMARY KEY,
  user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  game_id     BIGINT NOT NULL REFERENCES games(id) ON DELETE CASCADE,
  content     TEXT,
  image       TEXT,
  posted_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Helpful indexes
CREATE INDEX idx_playlists_user_id          ON playlists(user_id);
CREATE INDEX idx_playlist_items_music_id    ON playlist_items(music_id);
CREATE INDEX idx_tournaments_creator        ON tournaments(creator_id);
CREATE INDEX idx_tournaments_playlist       ON tournaments(playlist_id);
CREATE INDEX idx_games_creator_id           ON games(creator_id);
CREATE INDEX idx_games_playlist_id          ON games(playlist_id);
CREATE INDEX idx_games_played_at            ON games(played_at);
CREATE INDEX idx_musics_genre_id            ON musics(genre_id);
CREATE INDEX idx_musics_album_id            ON musics(album_id);
CREATE INDEX idx_albums_artist_id           ON albums(artist_id);
CREATE INDEX idx_posts_user_id              ON posts(user_id);
CREATE INDEX idx_posts_game_id              ON posts(game_id);
CREATE INDEX idx_posts_user_id_posted_at    ON posts(user_id, posted_at DESC);
CREATE INDEX idx_posts_game_id_posted_at    ON posts(game_id, posted_at DESC);