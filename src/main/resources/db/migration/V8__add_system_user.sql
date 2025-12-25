ALTER TABLE users
    ALTER COLUMN role TYPE VARCHAR(10);

UPDATE users
SET email = 'admin@musiguessr.app'
WHERE id = 1;

INSERT INTO users (id, name, username, email, password, role)
VALUES (
  0,
  'System',
  'system',
  'system@musiguessr.app',
  '$2a$10$ZnWZQeQWf8tgzG/DevN8hOXIhMN6YKQLpeUdBpSC30NUHcd9n5qMy',
  'SYSTEM'
);