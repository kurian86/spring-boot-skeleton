CREATE TABLE users (
    id              UUID PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    email           VARCHAR(320) NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT users_email_unique UNIQUE (email)
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);

COMMENT ON COLUMN users.status IS 'User status: ACTIVE or DISABLED';
