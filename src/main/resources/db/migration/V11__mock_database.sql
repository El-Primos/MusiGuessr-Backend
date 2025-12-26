INSERT INTO users (id, name, username, email, password, role, score)
    OVERRIDING SYSTEM VALUE
VALUES
    (10002, 'Kaan',  'kaan',  'kaan@musiguessr.app',
     '$2a$10$ZnWZQeQWf8tgzG/DevN8hOXIhMN6YKQLpeUdBpSC30NUHcd9n5qMy', 'ADMIN', 120),
    (10003, 'Kemal', 'kemal', 'kemal@musiguessr.app',
     '$2a$10$ZnWZQeQWf8tgzG/DevN8hOXIhMN6YKQLpeUdBpSC30NUHcd9n5qMy', 'ADMIN', 60),
    (10004, 'Alemre','alemre','alemre@musiguessr.app',
     '$2a$10$ZnWZQeQWf8tgzG/DevN8hOXIhMN6YKQLpeUdBpSC30NUHcd9n5qMy', 'ADMIN', 15)
    ON CONFLICT (id) DO NOTHING;

INSERT INTO followings (user_id, following_id, pending, is_accepted)
VALUES
    (10002, 10003, FALSE, TRUE),
    (10004, 10002, TRUE,  FALSE),
    (10004, 10003, FALSE, TRUE)
    ON CONFLICT (user_id, following_id) DO NOTHING;

INSERT INTO artists (id, name)
    OVERRIDING SYSTEM VALUE
VALUES
    (20001, 'Neon Skyline'),
    (20002, 'Midnight Lofi'),
    (20003, 'Anatolian Echo'),
    (20004, 'Motive'),
    (20005, 'Mavi Gri'),
    (20006, 'Son Feci Bisiklet'),
    (20007, 'Gece'),
    (20008, 'Mor ve Ötesi')
    ON CONFLICT (id) DO NOTHING;

INSERT INTO genres (id, name)
    OVERRIDING SYSTEM VALUE
VALUES
    (30001, 'Pop'),
    (30002, 'Rock'),
    (30003, 'Lo-fi'),
    (30004, 'Electronic'),
    (30005, 'Rap')
    ON CONFLICT (id) DO NOTHING;

INSERT INTO musics (id, name, genre_id, artist_id, url, owner_id, created_at)
    OVERRIDING SYSTEM VALUE
VALUES
    (40001, 'PVG',              30005, 20004, 'https://musiguessr-music-bucket.s3.eu-central-1.amazonaws.com/music/00773ed7-4b98-4f97-946a-e5b9ccda15d2', 10002, NOW() - INTERVAL '20 days'),
    (40002, 'Aklımı Kaçırdım',  30002, 20005, 'https://musiguessr-music-bucket.s3.eu-central-1.amazonaws.com/music/788e8d14-755b-4125-9117-be43cc96aaca', 10002, NOW() - INTERVAL '15 days'),
    (40003, 'Bu Kız',            30002, 20006, 'https://musiguessr-music-bucket.s3.eu-central-1.amazonaws.com/music/8cd16756-fdc2-402f-adb2-a8212bf29a76', 10003, NOW() - INTERVAL '12 days'),
    (40004, 'Ben Öldüm',         30002, 20007, 'https://musiguessr-music-bucket.s3.eu-central-1.amazonaws.com/music/68f9864d-b417-4af4-ac90-c5827afbab57', 10004, NOW() - INTERVAL '12 days'),
    (40005, '80',                30002, 20006, 'https://musiguessr-music-bucket.s3.eu-central-1.amazonaws.com/music/842fcab8-90db-4d6a-b888-e77048c45e31', 10004, NOW() - INTERVAL '10 days'),
    (40006, 'Cambaz',            30002, 20008, 'https://musiguessr-music-bucket.s3.eu-central-1.amazonaws.com/music/8dd6248c-8707-497d-8cbc-97ea1a7e6eb1', 10004, NOW() - INTERVAL '5 days')
    ON CONFLICT (id) DO NOTHING;

INSERT INTO playlists (id, name, owner_id, created_at)
    OVERRIDING SYSTEM VALUE
VALUES
    (50001, 'Daily Mix',        10002, NOW() - INTERVAL '14 days'),
    (50002, 'Tournament Picks', 10004, NOW() - INTERVAL '13 days'),
    (50003, 'Lofi Focus',       10003, NOW() - INTERVAL '9 days')
    ON CONFLICT (id) DO NOTHING;

INSERT INTO playlist_items (playlist_id, position, music_id)
VALUES
    (50001, 1, 40001),
    (50001, 2, 40002),
    (50001, 3, 40006),
    (50001, 4, 40003)
    ON CONFLICT DO NOTHING;

INSERT INTO playlist_items (playlist_id, position, music_id)
VALUES
    (50002, 1, 40002),
    (50002, 2, 40001),
    (50002, 3, 40003),
    (50002, 4, 40004),
    (50002, 5, 40005),
    (50002, 6, 40006)
    ON CONFLICT DO NOTHING;

INSERT INTO playlist_items (playlist_id, position, music_id)
VALUES
    (50003, 1, 40004),
    (50003, 2, 40005),
    (50003, 3, 40001)
    ON CONFLICT DO NOTHING;

INSERT INTO tournaments (id, owner_id, playlist_id, name, description, created_at, start_date, end_date, state)
    OVERRIDING SYSTEM VALUE
VALUES
    (60001, 10002, 50002, 'Winter Clash',
     'Seasonal bracket using Tournament Picks.',
     NOW() - INTERVAL '8 days',
     NOW() + INTERVAL '2 days',
     NOW() + INTERVAL '9 days',
     'UPCOMING'),

    (60002, 10002, 50001, 'Speedrun Saturday',
     'Short tournament: quick guesses, quick points.',
     NOW() - INTERVAL '6 days',
     NOW() - INTERVAL '1 day',
     NOW() + INTERVAL '1 day',
     'ACTIVE'),

    (60003, 10002, 50001, 'Alice Invitational',
     'Friends-only mini cup.',
     NOW() - INTERVAL '20 days',
     NOW() - INTERVAL '18 days',
     NOW() - INTERVAL '17 days',
     'FINISHED'),

    (60004, 10003, 50003, 'Lofi League',
     'Chill rounds, focus on accuracy.',
     NOW() - INTERVAL '4 days',
     NOW() + INTERVAL '1 day',
     NOW() + INTERVAL '6 days',
     'UPCOMING')
    ON CONFLICT (id) DO NOTHING;

INSERT INTO tournament_info (tournament_id, user_id, user_score)
VALUES
    (60001, 10002, 100),
    (60001, 10003, 100),
    (60001, 10004, 100),
    (60002, 10002, 115),
    (60002, 10003, 110),
    (60002, 10004, 105),
    (60003, 10002, 105),
    (60003, 10003, 104),
    (60004, 10002, 100),
    (60004, 10003, 100)
    ON CONFLICT (tournament_id, user_id) DO NOTHING;

INSERT INTO games (id, owner_id, playlist_id, state, created_at)
    OVERRIDING SYSTEM VALUE
VALUES
    (70001, 10002, 50001, 'CREATED', NOW() - INTERVAL '3 days'),
    (70002, 10003, 50003, 'CREATED', NOW() - INTERVAL '2 days'),
    (70003, 10004, 50002, 'CREATED', NOW() - INTERVAL '1 day')
    ON CONFLICT (id) DO NOTHING;

INSERT INTO game_history (id, game_id, user_id, score)
    OVERRIDING SYSTEM VALUE
VALUES
    (80001, 70001, 10002, 120),
    (80002, 70001, 10003, 80),
    (80003, 70002, 10003, 95),
    (80004, 70002, 10002, 70),
    (80005, 70003, 10004, 60)
    ON CONFLICT (id) DO NOTHING;

INSERT INTO game_rounds (game_history_id, song, guessed_song, guess_time, score_earned, round, guessed)
VALUES
    -- GH 80001 (Kaan)
    (80001, 'City Lights',    'City Lights',    3400, 50, 1, TRUE),
    (80001, 'Paper Planes',   'Paper Planes',   4100, 40, 2, TRUE),
    (80001, 'Bosporus Drift', NULL,             9000, 30, 3, FALSE),

    -- GH 80002 (Kemal)
    (80002, 'City Lights',    'Granite Heart',  7200, 20, 1, TRUE),
    (80002, 'Paper Planes',   NULL,             9500, 30, 2, FALSE),
    (80002, 'Bosporus Drift', 'Bosporus Drift', 5000, 30, 3, TRUE),

    -- GH 80003 (Kemal)
    (80003, 'Night Study',    'Night Study',    3000, 45, 1, TRUE),
    (80003, 'Rain on Vinyl',  'Rain on Vinyl',  3500, 30, 2, TRUE),
    (80003, 'City Lights',    NULL,             9800, 20, 3, FALSE),

    -- GH 80004 (Kaan)
    (80004, 'Night Study',    NULL,             9900, 20, 1, FALSE),
    (80004, 'Rain on Vinyl',  'Rain on Vinyl',  4200, 25, 2, TRUE),
    (80004, 'City Lights',    'City Lights',    3800, 25, 3, TRUE),

    -- GH 80005 (Alemre)
    (80005, 'Paper Planes',   'Paper Planes',   4500, 25, 1, TRUE),
    (80005, 'Granite Heart',  NULL,             9900, 15, 2, FALSE),
    (80005, 'Rain on Vinyl',  'Rain on Vinyl',  4700, 20, 3, TRUE)
    ON CONFLICT DO NOTHING;

INSERT INTO posts (id, user_id, game_history_id, posted_at)
    OVERRIDING SYSTEM VALUE
VALUES
    (90001, 10002, 80001, NOW() - INTERVAL '3 days' + INTERVAL '2 hours'),
    (90002, 10003, 80002, NOW() - INTERVAL '3 days' + INTERVAL '3 hours'),
    (90003, 10003, 80003, NOW() - INTERVAL '2 days' + INTERVAL '1 hour'),
    (90004, 10004, 80005, NOW() - INTERVAL '1 day' + INTERVAL '4 hours')
    ON CONFLICT (id) DO NOTHING;

INSERT INTO refresh_tokens (id, user_id, token, expiry_date)
    OVERRIDING SYSTEM VALUE
VALUES
    (95001, 10002, 'dev_refresh_kaan_00000000000000000000000000000001', NOW() + INTERVAL '30 days'),
    (95002, 10003, 'dev_refresh_kemal_00000000000000000000000000000002', NOW() + INTERVAL '30 days')
    ON CONFLICT (id) DO NOTHING;
