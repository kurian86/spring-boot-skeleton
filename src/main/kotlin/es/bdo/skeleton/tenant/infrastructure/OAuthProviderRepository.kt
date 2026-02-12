package es.bdo.skeleton.tenant.infrastructure

import es.bdo.skeleton.tenant.domain.OAuthProvider
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
            name = rs.getString("name"),
            issuer = rs.getString("issuer"),
            jwkSetUri = rs.getString("jwk_set_uri"),
            isOpaque = rs.getBoolean("is_opaque"),
            isActive = rs.getBoolean("is_active"),
            createdAt = rs.getObject("created_at", java.time.OffsetDateTime::class.java).toZonedDateTime(),
            updatedAt = rs.getObject("updated_at", java.time.OffsetDateTime::class.java).toZonedDateTime()
        )
    }
    
    override fun findAllByTenantId(tenantId: String): List<OAuthProvider> {
        val sql = """
            SELECT id, tenant_id, name, issuer, jwk_set_uri, is_opaque, is_active, created_at, updated_at 
            FROM tenants_oauth_providers
            WHERE tenant_id = :tenantId
            ORDER BY name
        """.trimIndent()
        val params = mapOf("tenantId" to tenantId)

        return jdbcTemplate.query(sql, params, rowMapper)
    }

    override fun findActiveByTenantId(tenantId: String): List<OAuthProvider> {
        val sql = """
            SELECT id, tenant_id, name, issuer, jwk_set_uri, is_opaque, is_active, created_at, updated_at 
            FROM tenants_oauth_providers
            WHERE tenant_id = :tenantId AND is_active = :isActive
            ORDER BY name
        """.trimIndent()
        val params = mapOf("tenantId" to tenantId, "isActive" to true)

        return jdbcTemplate.query(sql, params, rowMapper)
    }

    override fun findByTenantIdAndIssuer(
        tenantId: String,
        issuer: String
    ): OAuthProvider? {
        val sql = """
            SELECT id, tenant_id, name, issuer, jwk_set_uri, is_opaque, is_active, created_at, updated_at 
            FROM tenants_oauth_providers
            WHERE tenant_id = :tenantId AND issuer = :issuer AND is_active = :isActive
        """.trimIndent()
        val params = mapOf(
            "tenantId" to tenantId,
            "issuer" to issuer,
            "isActive" to true
        )

        return jdbcTemplate.query(sql, params, rowMapper).firstOrNull()
    }
}
