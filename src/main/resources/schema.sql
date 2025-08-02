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