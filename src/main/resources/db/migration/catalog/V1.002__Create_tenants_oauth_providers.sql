CREATE TABLE IF NOT EXISTS tenants_oauth_providers (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id         VARCHAR(50)   NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    type              VARCHAR(50)   NOT NULL,
    name              VARCHAR(100)  NOT NULL,
    client_id         VARCHAR(255)  NOT NULL,
    client_secret     VARCHAR(500)  NOT NULL,
    issuer            VARCHAR(255)  NOT NULL,
    authorization_uri VARCHAR(500),
    token_uri         VARCHAR(500),
    user_info_uri     VARCHAR(500),
    jwk_set_uri       VARCHAR(500)  NOT NULL,
    scope             VARCHAR(255),
    is_opaque         BOOLEAN       NOT NULL DEFAULT FALSE,
    is_active         BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, type)
);

CREATE INDEX IF NOT EXISTS idx_oauth_providers_tenant ON tenants_oauth_providers(tenant_id);
CREATE INDEX IF NOT EXISTS idx_oauth_providers_active ON tenants_oauth_providers(tenant_id, is_active);

COMMENT ON TABLE tenants_oauth_providers IS 'OAuth2 provider configurations per tenant. Supports multiple providers (GitHub, Google, etc.) per tenant.';
COMMENT ON COLUMN tenants_oauth_providers.type IS 'Provider type: GITHUB, GOOGLE, AZURE, etc.';
COMMENT ON COLUMN tenants_oauth_providers.client_secret IS 'OAuth2 client secret (should be encrypted)';
COMMENT ON COLUMN tenants_oauth_providers.scope IS 'Comma-separated OAuth2 scopes';

INSERT INTO tenants_oauth_providers (
    tenant_id,
    type,
    name,
    client_id,
    client_secret,
    issuer,
    authorization_uri,
    token_uri,
    user_info_uri,
    jwk_set_uri,
    scope,
    is_opaque
) VALUES (
    'default',
    'GITHUB',
    'GitHub',
    'your-github-client-id',
    'your-github-client-secret',
    'https://github.com/login/oauth',
    'https://github.com/login/oauth/authorize',
    'https://github.com/login/oauth/access_token',
    'https://api.github.com/user',
    'https://token.actions.githubusercontent.com/.well-known/jwks',
    'read:user,user:email',
    true
) ON CONFLICT (tenant_id, type) DO NOTHING;
