CREATE TABLE tenants_config (
    id                UUID PRIMARY KEY,
    tenant_id         VARCHAR(50) NOT NULL UNIQUE REFERENCES tenants(id) ON DELETE CASCADE,
    allowed_domains   TEXT[]      NOT NULL,
    primary_color     VARCHAR(7),
    secondary_color   VARCHAR(7),
    logo_url          VARCHAR(500),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tenants_config_tenant_id ON tenants_config(tenant_id);

COMMENT ON TABLE tenants_config IS 'Tenant-specific configuration for registration domain validation and UI customization';
COMMENT ON COLUMN tenants_config.allowed_domains IS 'Email domains allowed to register for this tenant (e.g., [''example.com'', ''acme.org''])';

INSERT INTO tenants_config (tenant_id, allowed_domains, primary_color, secondary_color)
VALUES ('default', ARRAY['example.com', 'localhost'], '#3498DB', '#2ECC71')
ON CONFLICT (tenant_id) DO NOTHING;
