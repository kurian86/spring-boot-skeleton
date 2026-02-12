package es.bdo.skeleton.tenant.infrastructure

import es.bdo.skeleton.tenant.domain.OAuthProvider
import es.bdo.skeleton.tenant.domain.ProviderType
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.util.*
import es.bdo.skeleton.tenant.domain.OAuthProviderRepository as IOAuthProviderRepository

@Repository
class OAuthProviderRepository(
    private val jdbcTemplate: NamedParameterJdbcTemplate
) : IOAuthProviderRepository {

    private val rowMapper = RowMapper { rs, _ ->
        OAuthProvider(
            id = UUID.fromString(rs.getString("id")),
            tenantId = rs.getString("tenant_id"),
            type = ProviderType.valueOf(rs.getString("type")),
            name = rs.getString("name"),
            clientId = rs.getString("client_id"),
            clientSecret = rs.getString("client_secret"),
            issuer = rs.getString("issuer"),
            authorizationUri = rs.getString("authorization_uri"),
            tokenUri = rs.getString("token_uri"),
            userInfoUri = rs.getString("user_info_uri"),
            jwkSetUri = rs.getString("jwk_set_uri"),
            scope = rs.getString("scope"),
            isOpaque = rs.getBoolean("is_opaque"),
            isActive = rs.getBoolean("is_active"),
            createdAt = rs.getObject("created_at", java.time.OffsetDateTime::class.java).toZonedDateTime(),
            updatedAt = rs.getObject("updated_at", java.time.OffsetDateTime::class.java).toZonedDateTime()
        )
    }
    
    override fun findAllByTenantId(tenantId: String): List<OAuthProvider> {
        val sql = """
            SELECT id, tenant_id, type, name, client_id, client_secret, issuer, authorization_uri, token_uri,
                   user_info_uri, jwk_set_uri, scope, is_opaque, is_active, created_at, updated_at 
            FROM tenants_oauth_providers
            WHERE tenant_id = :tenantId
            ORDER BY name
        """.trimIndent()
        val params = mapOf("tenantId" to tenantId)

        return jdbcTemplate.query(sql, params, rowMapper)
    }

    override fun findActiveByTenantId(tenantId: String): List<OAuthProvider> {
        val sql = """
            SELECT id, tenant_id, type, name, client_id, client_secret, issuer, authorization_uri, token_uri,
                   user_info_uri, jwk_set_uri, scope, is_opaque, is_active, created_at, updated_at 
            FROM tenants_oauth_providers
            WHERE tenant_id = :tenantId AND is_active = :isActive
            ORDER BY name
        """.trimIndent()
        val params = mapOf("tenantId" to tenantId, "isActive" to true)

        return jdbcTemplate.query(sql, params, rowMapper)
    }

    override fun findByTenantIdAndType(
        tenantId: String,
        providerType: ProviderType
    ): OAuthProvider? {
        val sql = """
            SELECT id, tenant_id, type, name, client_id, client_secret, issuer, authorization_uri, token_uri,
                   user_info_uri, jwk_set_uri, scope, is_opaque, is_active, created_at, updated_at 
            FROM tenants_oauth_providers
            WHERE tenant_id = :tenantId AND type = :type AND is_active = :isActive
        """.trimIndent()
        val params = mapOf(
            "tenantId" to tenantId,
            "type" to providerType.name,
            "isActive" to true
        )

        return jdbcTemplate.query(sql, params, rowMapper).firstOrNull()
    }
}
