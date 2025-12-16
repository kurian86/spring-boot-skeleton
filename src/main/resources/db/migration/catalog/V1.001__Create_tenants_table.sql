CREATE TABLE tenants
(
    id          VARCHAR(50) PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    db_database VARCHAR(255) NOT NULL,
    db_username VARCHAR(100) NOT NULL,
    db_password VARCHAR(500) NOT NULL,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON COLUMN tenants.db_password IS 'Encrypted password using AES-256-GCM. Store only encrypted values.';

INSERT INTO tenants (id, name, db_database, db_username, db_password, is_active)
VALUES ('default', 'Default', 'default', 'user', 'V1EP77Ev/JOQh2ZyOgm9onJRKkm+g2HLXu8G/tSfaPliW/vg', TRUE);
