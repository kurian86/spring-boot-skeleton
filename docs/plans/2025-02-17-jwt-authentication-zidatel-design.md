# JWT Authentication with Zidatel IDP

**Date:** 2025-02-17  
**Status:** Approved  
**Author:** Assistant

## Overview

Update the authentication system to work with Zidatel IDP, which now provides JWT tokens containing tenant information and user roles. The system will validate tenant IDs against headers, extract roles directly from JWT claims, and automatically register users on first authentication.

## JWT Structure

Example JWT payload from Zidatel IDP:

```json
{
  "at_hash": "lq6gquxHX6isSLU8npuLxg",
  "aud": [
    "360432643210280963",
    "360432808247754755",
    "360431956569096195"
  ],
  "auth_time": 1771345328,
  "azp": "360432643210280963",
  "client_id": "360432643210280963",
  "email": "kurian.86@gmail.com",
  "email_verified": true,
  "exp": 1771388650,
  "family_name": "Siles López",
  "given_name": "Ángel María",
  "iat": 1771345450,
  "iss": "http://localhost:8080",
  "locale": null,
  "name": "Ángel María Siles López",
  "preferred_username": "kurian.86@gmail.com",
  "roles": ["user", "admin"],
  "sid": "360457358565965826",
  "sub": "360435972631953411",
  "tenant_id": "default",
  "updated_at": 1771339188
}
```

## Changes

### 1. TenantContextFilter

**Purpose:** Extract `tenant_id` from JWT and validate against `X-Tenant-ID` header.

**Implementation:**
- Parse JWT (without signature verification) using `JWTParser.parse(token).jwtClaimsSet`
- Extract `tenant_id` claim → e.g., "default"
- Compare with `X-Tenant-ID` header value
- Return `403 Forbidden` if they don't match
- Continue with `TenantContext.withTenant(tenantId)` if valid

### 2. TenantJwtAuthenticationConverter

**Purpose:** Extract roles from JWT and handle automatic user registration.

**UserInfo Extraction:**
| Claim | Field | Example Value |
|-------|-------|---------------|
| `sub` | subject | "360435972631953411" |
| `preferred_username` | username | "kurian.86@gmail.com" |
| `email` | email | "kurian.86@gmail.com" |
| `name` | display name | "Ángel María Siles López" |

**Authority Extraction:**
- Extract `roles` claim: `["user", "admin"]`
- Convert to `GrantedAuthority` with `ROLE_` prefix: `ROLE_USER`, `ROLE_ADMIN`
- No database lookup for roles (they come from JWT)

**Automatic User Registration:**
- Search for user by email in database
- If not found, create new user with:
  - `id`: Generated UUID
  - `name`: `name` claim or `preferred_username` as fallback
  - `email`: `email` claim
  - `externalId`: `sub` claim (optional reference)
  - `status`: ACTIVE
  - `createdAt`/`updatedAt`: Current timestamp
- No domain validation (users can register from any domain)

### 3. Code Removal

Remove the following components (no longer needed):

- `UserProvider.findUserAuthoritiesByEmail()` - roles come from JWT
- `RegisterUserCommand` and `RegisterUserCommandHandler` - registration is automatic
- Domain validation logic in tenant configuration

## Security Considerations

1. **Tenant Validation:** The `tenant_id` in the JWT is validated against the header to prevent cross-tenant access with stolen tokens
2. **Role Trust:** Roles are fully trusted from the JWT since they come from the authoritative IDP
3. **Automatic Registration:** Users are created on first authentication without manual approval
4. **Token Validation:** JWT signature and expiration are still validated by Spring Security's OAuth2 resource server

## Testing Strategy

1. **Unit Tests:**
   - TenantContextFilter with matching/non-matching tenant IDs
   - TenantJwtAuthenticationConverter with various JWT claim combinations
   - User auto-registration when user doesn't exist

2. **Integration Tests:**
   - End-to-end authentication flow with sample JWT
   - Cross-tenant access prevention
   - Role extraction and authority assignment

## Migration Notes

- Existing users in database remain valid
- New users will be created automatically on their first login
- No migration of user roles needed (roles come from JWT)
- Remove any manual user registration endpoints

## Configuration

No changes needed to `application.yml`. Spring Boot's standard OAuth2 resource server configuration continues to work:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080
          jwk-set-uri: http://localhost:8080/oauth/v2/keys
```
