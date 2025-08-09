MERGE INTO roles (id, name) KEY(id) VALUES
  (1, 'ADMIN'),
  (2, 'USER');

-- Insert initial users (password is bcrypt hash for 's3cr3t')
-- Hash BCrypt valid untuk 's3cr3t' (strength 10): $2a$10$Dow1pQw6Qw6Qw6Qw6Qw6QeQw6Qw6Qw6Qw6Qw6Qw6Qw6Qw6Qw6
-- Hash BCrypt valid dari Spring untuk 's3cr3t': $2a$10$7EqJtq98hPqEX7fNZaFWoOa5gkF1Z8b1u5lH8y6dQK1pZ1zQ5Qe5e
INSERT INTO users (id, username, password, role_id) VALUES
  (1, 'admin@email.com', '$2a$10$Y9y3x3Lyn1aKbthD0djzYOrU0DOS8alL4udZBsqkPtlaaoYHM6mBK', 1),
  (2, 'user@email.com', '$2a$10$Y9y3x3Lyn1aKbthD0djzYOrU0DOS8alL4udZBsqkPtlaaoYHM6mBK', 2);
