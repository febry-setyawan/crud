-- Tambahkan kolom role_id ke tabel users
ALTER TABLE users ADD COLUMN role_id BIGINT;

-- Buat foreign key constraint yang merujuk ke tabel roles
ALTER TABLE users ADD CONSTRAINT fk_users_roles FOREIGN KEY (role_id) REFERENCES roles(id);

-- Opsional: Anda bisa membuat kolom ini NOT NULL jika setiap user wajib memiliki role
ALTER TABLE users ALTER COLUMN role_id SET NOT NULL;