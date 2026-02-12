package es.bdo.skeleton.tenant.infrastructure

import es.bdo.skeleton.tenant.domain.TenantRepository as ITenantRepository
import es.bdo.skeleton.tenant.domain.Tenant
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class TenantRepository(
    private val jdbcTemplate: NamedParameterJdbcTemplate
) : ITenantRepository {

    private val rowMapper = RowMapper { rs, _ ->
        Tenant(
            id = rs.getString("id"),
            name = rs.getString("name"),
            dbDatabase = rs.getString("db_database"),
            dbUsername = rs.getString("db_username"),
            dbPassword = rs.getString("db_password"),
            isActive = rs.getBoolean("is_active"),
            createdAt = rs.getObject("created_at", java.time.OffsetDateTime::class.java).toZonedDateTime(),
            updatedAt = rs.getObject("updated_at", java.time.OffsetDateTime::class.java).toZonedDateTime()
        )
    }

    override fun findAllActive(): List<Tenant> {
        val sql = """
            SELECT id, name, db_database, db_username, db_password, is_active, created_at, updated_at FROM tenants
            WHERE is_active = :isActive
        """.trimIndent()
        val params = mapOf("isActive" to true)

        return jdbcTemplate.query(sql, params, rowMapper)
    }

    override fun findById(id: String): Tenant? {
        val sql = """
            SELECT id, name, db_database, db_username, db_password, is_active, created_at, updated_at FROM tenants
            WHERE id = :id
        """.trimIndent()
        val params = mapOf("id" to id)

        return jdbcTemplate.query(sql, params, rowMapper).firstOrNull()
    }
}
