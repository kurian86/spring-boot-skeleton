CREATE TABLE tenants_configs
(
    tenant_id   VARCHAR(50) PRIMARY KEY,
    tenant_name VARCHAR(100) NOT NULL,
    db_url      VARCHAR(255) NOT NULL,
    db_username VARCHAR(100) NOT NULL,
    db_password VARCHAR(100) NOT NULL,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
