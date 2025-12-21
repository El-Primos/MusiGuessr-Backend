-- Add status column to tournaments table
ALTER TABLE tournaments
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'UPCOMING';

-- Create index for status queries
CREATE INDEX idx_tournaments_status ON tournaments(status);
