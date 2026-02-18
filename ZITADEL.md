# ZITADEL Configuration Guide

This document contains configuration tips, common issues, and solutions for ZITADEL self-hosted setup.

## Table of Contents

- [Reverse Proxy Configuration](#reverse-proxy-configuration)
- [Actions](#actions)
- [Identity Providers](#identity-providers)
- [Organization Scopes](#organization-scopes)
- [Common Issues & Solutions](#common-issues--solutions)

---

## Reverse Proxy Configuration

### The Port 443 Problem

When using a reverse proxy (Traefik, Nginx, Caddy, etc.), ZITADEL may incorrectly include `:443` in callback URLs (e.g., `https://your-domain.com:443/idps/callback`), which causes OAuth providers like Google to reject the authentication with `invalid_grant: Bad Request`.

This happens because the reverse proxy passes the `X-Forwarded-Host` header with the port included (e.g., `your-domain.com:443`), and ZITADEL uses this directly to construct callback URLs.

### Solution: Force Headers Without Port

Configure your reverse proxy to override the `X-Forwarded-Host` header and explicitly set the port-related headers:

**Generic headers to set:**
- `X-Forwarded-Host`: Your domain without port (e.g., `your-domain.com`)
- `X-Forwarded-Port`: `443`
- `X-Forwarded-Proto`: `https`

**Example Docker Compose environment:**
```yaml
services:
  auth:
    image: ghcr.io/zitadel/zitadel:v4.11.0
    environment:
      - ZITADEL_EXTERNALDOMAIN=your-domain.com
      - ZITADEL_EXTERNALPORT=443
      - ZITADEL_EXTERNALSECURE=true
    # Add reverse proxy labels/config here
```

### Separate Login Service

If using the separate login service container, ensure it shares the network with the auth service:

```yaml
  login:
    image: ghcr.io/zitadel/zitadel-login:v4.11.0
    env_file: .login.env
    volumes:
      - ./zitadel-data:/current-dir:ro
    network_mode: service:auth  # Shares network with auth service
```

---

## Actions

### Complement Token Flow Actions

These actions run when generating access tokens to add custom claims.

#### Add Roles Claim

```javascript
function addClaimRoles(ctx, api) {
    const roles = ['user'];
    const grants = ctx.v1.user.grants;
    
    if (grants && grants.grants) {
        grants.grants.forEach(grant => {
            if (grant.roles) {
                grant.roles.forEach(role => {
                    roles.push(role);
                });
            }
        });
    }
    api.v1.claims.setClaim('roles', roles);
}
```

**Trigger:** Complement Token → Pre Userinfo creation

#### Add Tenant ID from Organization Metadata

Ensure that you have set the tenant ID in your organization's metadata.

```javascript
function addClaimTenantId(ctx, api) {
    const metadata = ctx.v1.org.getMetadata();
    const tenantId = metadata.metadata.find(it => it.key === 'tenantId');
    
    if (!tenantId) {
        throw new Error('Tenant ID is required. Please set Tenant ID in organizations metadata.');
    }
    
    api.v1.claims.setClaim('tenant_id', tenantId.value)
}
```

**Trigger:** Complement Token → Pre Userinfo creation

---

## Identity Providers

### GitHub

**Issue:** GitHub doesn't provide first/last name by default, causing `user_creation_failed` errors.

**Solution:**
1. Disable "Automatic creation" in the GitHub IdP settings
2. Enable "Account creation allowed (manually)"
3Let users complete the registration form manually

**Scopes:** Default `openid profile email` is sufficient

### Google

**Configuration in Google Cloud Console:**
- Authorized redirect URI: `https://your-domain.com/idps/callback`
- **Important:** Do NOT include `:443` in the URI
- Application type: Web application

**Scopes:** `openid profile email`

---

## Organization Scopes

To redirect users to your organization's custom login page (instead of the default ZITADEL login), add the organization scope to your OIDC request:

```typescript
const ZITADEL_SCOPES = [
    'openid', 
    'profile', 
    'email', 
    'urn:zitadel:iam:org:id:YOUR_ORG_ID'
].join(' ');

const config = {
    authority: 'https://your-domain.com',
    client_id: 'YOUR_CLIENT_ID',
    redirect_uri: 'http://localhost:5173/callback',
    scope: ZITADEL_SCOPES,
    // ... other config
};
```

**Note:** Replace `YOUR_ORG_ID` with your actual organization ID

---

## Common Issues & Solutions

### "invalid_grant: Bad Request" with Google

**Cause:** ZITADEL includes `:443` in the callback URL.

**Solution:** Configure your reverse proxy to override `X-Forwarded-Host` without the port (see [Reverse Proxy Configuration](#reverse-proxy-configuration)).

### "user_creation_failed" with GitHub

**Cause:** GitHub doesn't provide `GivenName` (first name) required by ZITADEL.

**Solutions:**
1. Disable auto-creation, enable manual registration
2. Use an Action to set names from `providerInfo.login` or `providerInfo.name`
3. Configure GitHub OAuth app to request additional scopes (limited effectiveness)

---

## Additional Resources

- [ZITADEL Documentation](https://zitadel.com/docs)
- [Actions Repository](https://github.com/zitadel/actions)
- [OIDC Scopes Reference](https://zitadel.com/docs/apis/openidoauth/scopes)
- [External Authentication Flow](https://zitadel.com/docs/apis/actions/external-authentication)
