-- Add tournament_id column to games table to link games with tournaments
ALTER TABLE games
    ADD COLUMN tournament_id BIGINT REFERENCES tournaments(id) ON DELETE SET NULL;

-- Add index for better query performance
CREATE INDEX idx_games_tournament_id ON games(tournament_id);

