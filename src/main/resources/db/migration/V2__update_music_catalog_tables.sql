ALTER TABLE musics
    DROP COLUMN IF EXISTS album_id,
    DROP COLUMN IF EXISTS release_date,
    DROP COLUMN IF EXISTS language,
    ADD COLUMN artist_id BIGINT REFERENCES artists(id) ON DELETE SET NULL;

ALTER TABLE genre
    RENAME TO genres;

ALTER TABLE genres
    RENAME COLUMN genre TO name;

DROP TABLE IF EXISTS albums;