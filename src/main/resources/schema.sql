-- Hapus tabel jika sudah ada, untuk memastikan test selalu berjalan di environment bersih
DROP TABLE IF EXISTS users;

-- Buat tabel baru
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    -- Kolom Audit --
    created_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255)
);

-- Hapus tabel jika sudah ada, untuk memastikan test selalu berjalan di environment bersih
DROP TABLE IF EXISTS roles;

-- Buat tabel baru
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- Tambahkan kolom role_id ke tabel users
ALTER TABLE users ADD COLUMN role_id BIGINT;

-- Buat foreign key constraint yang merujuk ke tabel roles
ALTER TABLE users ADD CONSTRAINT fk_users_roles FOREIGN KEY (role_id) REFERENCES roles(id);

-- Opsional: Anda bisa membuat kolom ini NOT NULL jika setiap user wajib memiliki role
ALTER TABLE users ALTER COLUMN role_id SET NOT NULL;