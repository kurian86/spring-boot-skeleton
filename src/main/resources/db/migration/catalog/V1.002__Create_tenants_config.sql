CREATE TABLE tenants_config (
    id                UUID PRIMARY KEY,
    tenant_id         VARCHAR(50) NOT NULL UNIQUE REFERENCES tenants(id) ON DELETE CASCADE,
    primary_color     VARCHAR(7),
    secondary_color   VARCHAR(7),
    logo_url          VARCHAR(500),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tenants_config_tenant_id ON tenants_config(tenant_id);

COMMENT ON TABLE tenants_config IS 'Tenant-specific configuration for registration domain validation and UI customization';

INSERT INTO tenants_config (
    id,
    tenant_id,
    primary_color,
    secondary_color
)
VALUES (
    '019c660d-9919-7119-8423-f8ec56b5667b',
    'default',
    '#3498DB',
    '#2ECC71'
);
