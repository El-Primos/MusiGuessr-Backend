ALTER TABLE users
    RENAME COLUMN user_name TO username;

ALTER TABLE users
    ADD COLUMN role VARCHAR(5) NOT NULL DEFAULT 'USER';

INSERT INTO users (name, username, email, password, role)
VALUES (
  'Admin',
  'admin',
  'admin@musiguessr.com',
  '$2a$10$ZnWZQeQWf8tgzG/DevN8hOXIhMN6YKQLpeUdBpSC30NUHcd9n5qMy',
  'ADMIN'
);

ALTER TABLE tournaments
  DROP COLUMN IF EXISTS creator_id,
  ADD COLUMN creator_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT;

DROP TABLE IF EXISTS admins;