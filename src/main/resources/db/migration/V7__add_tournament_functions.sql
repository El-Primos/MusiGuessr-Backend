-- ============================================================
-- Prevent deleting a playlist if it is used by active tournaments
-- (state = UPCOMING or ACTIVE)
-- ============================================================
CREATE OR REPLACE FUNCTION prevent_playlist_delete_if_active_tournament()
RETURNS trigger AS $$
BEGIN
    -- Check whether the playlist is referenced by any active tournament
    IF EXISTS (
        SELECT 1
        FROM musiguessr_schema.tournaments t
        WHERE t.playlist_id = OLD.id
          AND t.state IN ('UPCOMING', 'ACTIVE')
    ) THEN
        -- Block deletion if active tournaments exist
        RAISE EXCEPTION
            'Cannot delete playlist %, it is used by upcoming or active tournaments',
            OLD.id
            USING ERRCODE = '23503';
END IF;

    -- Allow delete to proceed otherwise
RETURN OLD;
END;
$$ LANGUAGE plpgsql;


-- ============================================================
-- Trigger that runs before deleting a playlist
-- ============================================================
DROP TRIGGER IF EXISTS trg_prevent_playlist_delete ON playlists;

CREATE TRIGGER trg_prevent_playlist_delete
    BEFORE DELETE ON musiguessr_schema.playlists
    FOR EACH ROW
    EXECUTE FUNCTION prevent_playlist_delete_if_active_tournament();



ALTER TABLE tournaments
DROP CONSTRAINT tournaments_creator_id_fkey;

ALTER TABLE tournaments
    ADD CONSTRAINT tournaments_owner_id_fkey
        FOREIGN KEY (owner_id)
            REFERENCES users(id)
            ON DELETE SET NULL;


-- ============================================================
-- Prevent deleting a user if they are the owner of
-- active tournaments (state = UPCOMING or ACTIVE)
-- ============================================================
CREATE OR REPLACE FUNCTION prevent_user_delete_if_active_tournament()
RETURNS trigger AS $$
BEGIN
    -- Check whether the user is the owner of any active tournament
    IF EXISTS (
        SELECT 1
        FROM tournaments t
        WHERE t.owner_id = OLD.id
          AND t.state IN ('UPCOMING', 'ACTIVE')
    ) THEN
        -- Block deletion if active tournaments exist
        RAISE EXCEPTION
            'Cannot delete user %, owner of upcoming or active tournaments',
            OLD.id
            USING ERRCODE = '23503';
END IF;

    -- Allow delete to proceed otherwise
RETURN OLD;
END;
$$ LANGUAGE plpgsql;


-- ============================================================
-- Trigger that runs before deleting a user
-- ============================================================
CREATE TRIGGER trg_prevent_user_delete
    BEFORE DELETE ON users
    FOR EACH ROW
    EXECUTE FUNCTION prevent_user_delete_if_active_tournament();