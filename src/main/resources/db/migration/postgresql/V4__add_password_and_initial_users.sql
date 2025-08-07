-- Insert initial roles
INSERT INTO roles (id, name) VALUES
  (1, 'ADMIN'),
  (2, 'USER')
ON CONFLICT (id) DO NOTHING;

-- Insert initial users (password is bcrypt hash for 's3cr3t')
INSERT INTO users (id, name, email, password, role_id) VALUES
  (1, 'Admin', 'admin@email.com', '$2a$10$u1Qw6Qw6Qw6Qw6Qw6Qw6QeQw6Qw6Qw6Qw6Qw6Qw6Qw6Qw6Qw6', 1),
  (2, 'User', 'user@email.com', '$2a$10$u1Qw6Qw6Qw6Qw6Qw6Qw6QeQw6Qw6Qw6Qw6Qw6Qw6Qw6Qw6Qw6', 2)
ON CONFLICT (id) DO NOTHING;
