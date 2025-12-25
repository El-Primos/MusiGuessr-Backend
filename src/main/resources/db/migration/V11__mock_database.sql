BEGIN;

-- USERS
INSERT INTO users (id, name, username, email, password, role, score)
VALUES
    (2, 'Kaan ', 'kaan', 'kaan@musiguessr.app',
     '$2a$10$ZnWZQeQWf8tgzG/DevN8hOXIhMN6YKQLpeUdBpSC30NUHcd9n5qMy', 'USER', 120),
    (3, 'Kemal', 'kemal', 'kemal@musiguessr.app',
     '$2a$10$ZnWZQeQWf8tgzG/DevN8hOXIhMN6YKQLpeUdBpSC30NUHcd9n5qMy', 'USER', 60),
    (4, 'Alemre', 'alemre', 'alemre@musiguessr.app',
     '$2a$10$ZnWZQeQWf8tgzG/DevN8hOXIhMN6YKQLpeUdBpSC30NUHcd9n5qMy', 'USER', 15)
    ON CONFLICT (id) DO NOTHING;

INSERT INTO followings (user_id, following_id, pending, is_accepted)
VALUES
    (2, 3, FALSE, TRUE),
    (3, 2, FALSE, TRUE),
    (4, 2, TRUE,  FALSE),
    (4, 3, FALSE, TRUE)
    ON CONFLICT (user_id, following_id) DO NOTHING;

-- MUSIC CATALOG
INSERT INTO artists (id, name)
VALUES
    (1, 'Neon Skyline'),
    (2, 'Midnight Lofi'),
    (3, 'Anatolian Echo')
    ON CONFLICT (id) DO NOTHING;

INSERT INTO genres (id, name)
VALUES
    (1, 'Pop'),
    (2, 'Rock'),
    (3, 'Lo-fi'),
    (4, 'Electronic')
    ON CONFLICT (id) DO NOTHING;

-- 6 musics
INSERT INTO musics (id, name, genre_id, artist_id, url, owner_id, created_at)
VALUES
    (1, 'City Lights',     4, 1, 'https://cdn.example.com/audio/city-lights.mp3',     2, NOW() - INTERVAL '20 days'),
    (2, 'Paper Planes',    1, 1, 'https://cdn.example.com/audio/paper-planes.mp3',    2, NOW() - INTERVAL '15 days'),
    (3, 'Granite Heart',   2, 1, 'https://cdn.example.com/audio/granite-heart.mp3',   3, NOW() - INTERVAL '12 days'),
    (4, 'Night Study',     3, 2, 'https://cdn.example.com/audio/night-study.mp3',     3, NOW() - INTERVAL '10 days'),
    (5, 'Rain on Vinyl',   3, 2, 'https://cdn.example.com/audio/rain-on-vinyl.mp3',   4, NOW() - INTERVAL '7 days'),
    (6, 'Bosporus Drift',  4, 3, 'https://cdn.example.com/audio/bosporus-drift.mp3',  4, NOW() - INTERVAL '5 days')
    ON CONFLICT (id) DO NOTHING;

-- PLAYLISTS + ITEMS (2-3 playlists)
INSERT INTO playlists (id, name, owner_id, created_at)
    OVERRIDING SYSTEM VALUE
VALUES
    (1, 'Daily Mix',         2, NOW() - INTERVAL '14 days'),
    (2, 'Tournament Picks',  1, NOW() - INTERVAL '13 days'),
    (3, 'Lofi Focus',        3, NOW() - INTERVAL '9 days');

-- Daily Mix (Kaan): 4 tracks
INSERT INTO playlist_items (playlist_id, position, music_id)
VALUES
    (1, 1, 1),
    (1, 2, 2),
    (1, 3, 6),
    (1, 4, 3)
    ON CONFLICT DO NOTHING;

-- Tournament Picks (Admin): 6 tracks
INSERT INTO playlist_items (playlist_id, position, music_id)
VALUES
    (2, 1, 2),
    (2, 2, 1),
    (2, 3, 3),
    (2, 4, 4),
    (2, 5, 5),
    (2, 6, 6)
    ON CONFLICT DO NOTHING;

-- Lofi Focus (Kemal): 3 tracks
INSERT INTO playlist_items (playlist_id, position, music_id)
VALUES
    (3, 1, 4),
    (3, 2, 5),
    (3, 3, 1)
    ON CONFLICT DO NOTHING;

-- TOURNAMENTS
INSERT INTO tournaments (id, owner_id, playlist_id, name, description, created_at, start_date, end_date, state)
VALUES
    (1, 1, 2, 'Winter Clash',       'Seasonal bracket using Tournament Picks.', NOW() - INTERVAL '8 days',
     NOW() + INTERVAL '2 days',  NOW() + INTERVAL '9 days',  'UPCOMING'),
    (2, 1, 1, 'Speedrun Saturday',  'Short tournament: quick guesses, quick points.', NOW() - INTERVAL '6 days',
     NOW() - INTERVAL '1 day',   NOW() + INTERVAL '1 day',   'ACTIVE'),
    (3, 2, 1, 'Alice Invitational', 'Friends-only mini cup.', NOW() - INTERVAL '20 days',
     NOW() - INTERVAL '18 days', NOW() - INTERVAL '17 days', 'FINISHED'),
    (4, 3, 3, 'Lofi League',        'Chill rounds, focus on accuracy.', NOW() - INTERVAL '4 days',
     NOW() + INTERVAL '1 day',   NOW() + INTERVAL '6 days',  'UPCOMING')
    ON CONFLICT (id) DO NOTHING;

-- tournament participants/scores
INSERT INTO tournament_info (tournament_id, user_id, user_score)
VALUES
    (1, 2, 0),
    (1, 3, 0),
    (1, 4, 0),
    (2, 2, 15),
    (2, 3, 10),
    (2, 4, 5),
    (3, 2, 55),
    (3, 3, 40),
    (4, 2, 0),
    (4, 3, 0)
    ON CONFLICT (tournament_id, user_id) DO NOTHING;

-- GAMES + HISTORY + ROUNDS
INSERT INTO games (id, owner_id, playlist_id, state, created_at)
VALUES
    (1, 2, 1, 'CREATED', NOW() - INTERVAL '3 days'),
    (2, 3, 3, 'CREATED', NOW() - INTERVAL '2 days'),
    (3, 4, 2, 'CREATED', NOW() - INTERVAL '1 day')
    ON CONFLICT (id) DO NOTHING;

-- game_history: one per player per game
INSERT INTO game_history (id, game_id, user_id, score)
VALUES
    (1, 1, 2, 120),
    (2, 1, 3, 80),
    (3, 2, 3, 95),
    (4, 2, 2, 70),
    (5, 3, 4, 60)
    ON CONFLICT (id) DO NOTHING;

-- 3 rounds per history
INSERT INTO game_rounds (game_history_id, song, guessed_song, guess_time, score_earned, round, guessed)
VALUES
    -- GH 1 (Kaan)
    (1, 'City Lights',    'City Lights',    3400, 50, 1, TRUE),
    (1, 'Paper Planes',   'Paper Planes',   4100, 40, 2, TRUE),
    (1, 'Bosporus Drift', NULL,             9000, 30, 3, FALSE),

    -- GH 2 (Kemal)
    (2, 'City Lights',    'Granite Heart',  7200, 20, 1, TRUE),
    (2, 'Paper Planes',   NULL,             9500, 30, 2, FALSE),
    (2, 'Bosporus Drift', 'Bosporus Drift', 5000, 30, 3, TRUE),

    -- GH 3 (Kemal)
    (3, 'Night Study',    'Night Study',    3000, 45, 1, TRUE),
    (3, 'Rain on Vinyl',  'Rain on Vinyl',  3500, 30, 2, TRUE),
    (3, 'City Lights',    NULL,             9800, 20, 3, FALSE),

    -- GH 4 (Kaan)
    (4, 'Night Study',    NULL,             9900, 20, 1, FALSE),
    (4, 'Rain on Vinyl',  'Rain on Vinyl',  4200, 25, 2, TRUE),
    (4, 'City Lights',    'City Lights',    3800, 25, 3, TRUE),

    -- GH 5 (Alemre)
    (5, 'Paper Planes',   'Paper Planes',   4500, 25, 1, TRUE),
    (5, 'Granite Heart',  NULL,             9900, 15, 2, FALSE),
    (5, 'Rain on Vinyl',  'Rain on Vinyl',  4700, 20, 3, TRUE);

-- POSTS
INSERT INTO posts (id, user_id, game_history_id, posted_at)
VALUES
    (1, 2, 1, NOW() - INTERVAL '3 days' + INTERVAL '2 hours'),
    (2, 3, 2, NOW() - INTERVAL '3 days' + INTERVAL '3 hours'),
    (3, 3, 3, NOW() - INTERVAL '2 days' + INTERVAL '1 hour'),
    (4, 4, 5, NOW() - INTERVAL '1 day' + INTERVAL '4 hours')
    ON CONFLICT (id) DO NOTHING;

-- REFRESH TOKENS (dev-only)
INSERT INTO refresh_tokens (id, user_id, token, expiry_date)
VALUES
    (1, 2, 'dev_refresh_alice_00000000000000000000000000000001', NOW() + INTERVAL '30 days'),
    (2, 3, 'dev_refresh_bob__00000000000000000000000000000002', NOW() + INTERVAL '30 days')
    ON CONFLICT (id) DO NOTHING;

-- SEQUENCE FIXUP (so future inserts don’t collide)
SELECT setval(pg_get_serial_sequence('users', 'id'),        (SELECT COALESCE(MAX(id), 1) FROM users));
SELECT setval(pg_get_serial_sequence('artists', 'id'),      (SELECT COALESCE(MAX(id), 1) FROM artists));
SELECT setval(pg_get_serial_sequence('genres', 'id'),       (SELECT COALESCE(MAX(id), 1) FROM genres));
SELECT setval(pg_get_serial_sequence('musics', 'id'),       (SELECT COALESCE(MAX(id), 1) FROM musics));
SELECT setval(pg_get_serial_sequence('playlists', 'id'),    (SELECT COALESCE(MAX(id), 1) FROM playlists));
SELECT setval(pg_get_serial_sequence('tournaments', 'id'),  (SELECT COALESCE(MAX(id), 1) FROM tournaments));
SELECT setval(pg_get_serial_sequence('games', 'id'),        (SELECT COALESCE(MAX(id), 1) FROM games));
SELECT setval(pg_get_serial_sequence('game_history', 'id'), (SELECT COALESCE(MAX(id), 1) FROM game_history));
SELECT setval(pg_get_serial_sequence('game_rounds', 'id'),  (SELECT COALESCE(MAX(id), 1) FROM game_rounds));
SELECT setval(pg_get_serial_sequence('posts', 'id'),        (SELECT COALESCE(MAX(id), 1) FROM posts));
SELECT setval(pg_get_serial_sequence('refresh_tokens', 'id'), (SELECT COALESCE(MAX(id), 1) FROM refresh_tokens));

COMMIT;
