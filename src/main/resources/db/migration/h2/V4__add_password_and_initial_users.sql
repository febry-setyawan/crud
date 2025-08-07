MERGE INTO roles (id, name) KEY(id) VALUES
  (1, 'ADMIN'),
  (2, 'USER');

-- Insert initial users (password is bcrypt hash for 's3cr3t')
INSERT INTO users (id, username, password, role_id) VALUES
  (1, 'admin@email.com', '$2a$10$u1Qw6Qw6Qw6Qw6Qw6Qw6QeQw6Qw6Qw6Qw6Qw6Qw6Qw6Qw6Qw6', 1),
  (2, 'user@email.com', '$2a$10$u1Qw6Qw6Qw6Qw6Qw6Qw6QeQw6Qw6Qw6Qw6Qw6Qw6Qw6Qw6Qw6', 2);
