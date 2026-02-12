CREATE TABLE IF NOT EXISTS tenants_oauth_providers (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id         VARCHAR(50)   NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    name              VARCHAR(100)  NOT NULL,
    issuer            VARCHAR(255)  NOT NULL,
    jwk_set_uri       VARCHAR(500),
    is_opaque         BOOLEAN       NOT NULL DEFAULT FALSE,
    is_active         BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, issuer)
);

CREATE INDEX IF NOT EXISTS idx_oauth_providers_tenant ON tenants_oauth_providers(tenant_id);
CREATE INDEX IF NOT EXISTS idx_oauth_providers_active ON tenants_oauth_providers(tenant_id, is_active);

COMMENT ON TABLE tenants_oauth_providers IS 'OAuth2 provider configurations for resource server. Supports both JWT (requires jwk_set_uri) and opaque token (requires only issuer) validation.';
COMMENT ON COLUMN tenants_oauth_providers.issuer IS 'OAuth2 issuer URL - unique identifier for the provider. For JWT: used in token validation. For opaque: used for provider identification. Must be unique per tenant.';
COMMENT ON COLUMN tenants_oauth_providers.jwk_set_uri IS 'JWK Set URI for fetching public keys to verify JWT signatures. Required for JWT tokens, NULL for opaque tokens.';
COMMENT ON COLUMN tenants_oauth_providers.is_opaque IS 'Token type: true = opaque tokens (e.g., GitHub PAT), false = JWT tokens';

INSERT INTO tenants_oauth_providers (
    tenant_id,
    name,
    issuer,
    jwk_set_uri,
    is_opaque
) VALUES (
    'default',
    'GitHub',
    'https://github.com',
    NULL,
    true
) ON CONFLICT (tenant_id, issuer) DO NOTHING;
