package es.bdo.skeleton.tenant.infrastructure.model

import es.bdo.skeleton.tenant.domain.Config
import es.bdo.skeleton.tenant.domain.Tenant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import java.util.*

class EntityMappingTest {

    @Test
    fun `should map TenantEntity to Tenant domain with all fields`() {
        // Arrange
        val now = ZonedDateTime.now()
        val entity = TenantEntity(
            id = "tenant-123",
            name = "Test Tenant",
            dbDatabase = "test_db",
            dbUsername = "test_user",
            dbPassword = "encrypted_pass",
            isActive = true,
            createdAt = now,
            updatedAt = now
        )

        // Act
        val domain = entity.toDomain()

        // Assert
        assertThat(domain).isInstanceOf(Tenant::class.java)
        assertThat(domain.id).isEqualTo("tenant-123")
        assertThat(domain.name).isEqualTo("Test Tenant")
        assertThat(domain.dbDatabase).isEqualTo("test_db")
        assertThat(domain.dbUsername).isEqualTo("test_user")
        assertThat(domain.dbPassword).isEqualTo("encrypted_pass")
        assertThat(domain.isActive).isTrue()
        assertThat(domain.createdAt).isEqualTo(now)
        assertThat(domain.updatedAt).isEqualTo(now)
    }

    @Test
    fun `should map TenantEntity with inactive status`() {
        // Arrange
        val entity = TenantEntity(
            id = "tenant-456",
            name = "Inactive Tenant",
            dbDatabase = "inactive_db",
            dbUsername = "inactive_user",
            dbPassword = "pass",
            isActive = false,
            createdAt = ZonedDateTime.now(),
            updatedAt = ZonedDateTime.now()
        )

        // Act
        val domain = entity.toDomain()

        // Assert
        assertThat(domain.isActive).isFalse()
    }

    @Test
    fun `should map TenantEntity with special characters`() {
        // Arrange
        val entity = TenantEntity(
            id = "tenant-with_special.chars:123",
            name = "Special Tenant Name!@#",
            dbDatabase = "db-with-dashes",
            dbUsername = "user_name",
            dbPassword = "p@ssw0rd!",
            isActive = true,
            createdAt = ZonedDateTime.now(),
            updatedAt = ZonedDateTime.now()
        )

        // Act
        val domain = entity.toDomain()

        // Assert
        assertThat(domain.id).isEqualTo("tenant-with_special.chars:123")
        assertThat(domain.name).isEqualTo("Special Tenant Name!@#")
        assertThat(domain.dbPassword).isEqualTo("p@ssw0rd!")
    }

    @Test
    fun `should preserve exact timestamps when mapping TenantEntity`() {
        // Arrange
        val createdAt = ZonedDateTime.parse("2024-01-15T10:30:00+01:00[Europe/Madrid]")
        val updatedAt = ZonedDateTime.parse("2024-06-20T18:45:30+02:00[Europe/Paris]")
        val entity = TenantEntity(
            id = "tenant-timestamps",
            name = "Timestamp Tenant",
            dbDatabase = "ts_db",
            dbUsername = "ts_user",
            dbPassword = "ts_pass",
            isActive = true,
            createdAt = createdAt,
            updatedAt = updatedAt
        )

        // Act
        val domain = entity.toDomain()

        // Assert
        assertThat(domain.createdAt).isEqualTo(createdAt)
        assertThat(domain.updatedAt).isEqualTo(updatedAt)
    }

    @Test
    fun `should map ConfigEntity to Config domain with all fields`() {
        // Arrange
        val uuid = UUID.randomUUID()
        val entity = ConfigEntity(
            id = uuid,
            tenantId = "tenant-789",
            primaryColor = "#FF5733",
            secondaryColor = "#33FF57",
            logoUrl = "https://example.com/logo.png"
        )

        // Act
        val domain = entity.toDomain()

        // Assert
        assertThat(domain).isInstanceOf(Config::class.java)
        assertThat(domain.id).isEqualTo(uuid)
        assertThat(domain.tenantId).isEqualTo("tenant-789")
        assertThat(domain.primaryColor).isEqualTo("#FF5733")
        assertThat(domain.secondaryColor).isEqualTo("#33FF57")
        assertThat(domain.logoUrl).isEqualTo("https://example.com/logo.png")
    }

    @Test
    fun `should map ConfigEntity with null optional fields`() {
        // Arrange
        val uuid = UUID.randomUUID()
        val entity = ConfigEntity(
            id = uuid,
            tenantId = "tenant-minimal",
            primaryColor = null,
            secondaryColor = null,
            logoUrl = null
        )

        // Act
        val domain = entity.toDomain()

        // Assert
        assertThat(domain.id).isEqualTo(uuid)
        assertThat(domain.tenantId).isEqualTo("tenant-minimal")
        assertThat(domain.primaryColor).isNull()
        assertThat(domain.secondaryColor).isNull()
        assertThat(domain.logoUrl).isNull()
    }

    @Test
    fun `should preserve UUID when mapping ConfigEntity`() {
        // Arrange
        val specificUuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")
        val entity = ConfigEntity(
            id = specificUuid,
            tenantId = "tenant-uuid",
            primaryColor = "#123456",
            secondaryColor = null,
            logoUrl = null
        )

        // Act
        val domain = entity.toDomain()

        // Assert
        assertThat(domain.id).isEqualTo(specificUuid)
    }

    @Test
    fun `should map ConfigEntity with empty string values`() {
        // Arrange
        val entity = ConfigEntity(
            id = UUID.randomUUID(),
            tenantId = "tenant-empty",
            primaryColor = "",
            secondaryColor = "",
            logoUrl = ""
        )

        // Act
        val domain = entity.toDomain()

        // Assert
        assertThat(domain.primaryColor).isEqualTo("")
        assertThat(domain.secondaryColor).isEqualTo("")
        assertThat(domain.logoUrl).isEqualTo("")
    }

    @Test
    fun `should create independent domain objects from same entity`() {
        // Arrange
        val entity = TenantEntity(
            id = "tenant-shared",
            name = "Shared Tenant",
            dbDatabase = "shared_db",
            dbUsername = "shared_user",
            dbPassword = "shared_pass",
            isActive = true,
            createdAt = ZonedDateTime.now(),
            updatedAt = ZonedDateTime.now()
        )

        // Act
        val domain1 = entity.toDomain()
        val domain2 = entity.toDomain()

        // Assert
        assertThat(domain1).isEqualTo(domain2)
        assertThat(domain1).isNotSameAs(domain2)
    }

    @Test
    fun `should handle long strings in TenantEntity`() {
        // Arrange
        val longName = "A".repeat(1000)
        val longPassword = "B".repeat(500)
        val entity = TenantEntity(
            id = "tenant-long",
            name = longName,
            dbDatabase = "db",
            dbUsername = "user",
            dbPassword = longPassword,
            isActive = true,
            createdAt = ZonedDateTime.now(),
            updatedAt = ZonedDateTime.now()
        )

        // Act
        val domain = entity.toDomain()

        // Assert
        assertThat(domain.name).hasSize(1000)
        assertThat(domain.dbPassword).hasSize(500)
        assertThat(domain.name).isEqualTo(longName)
    }

    @Test
    fun `should handle Unicode characters in entity fields`() {
        // Arrange
        val entity = TenantEntity(
            id = "tenant-unicode",
            name = "Tenant with ñáéíóú 中文 🎉",
            dbDatabase = "db_unicode",
            dbUsername = "user_ñ",
            dbPassword = "pässwörd",
            isActive = true,
            createdAt = ZonedDateTime.now(),
            updatedAt = ZonedDateTime.now()
        )

        // Act
        val domain = entity.toDomain()

        // Assert
        assertThat(domain.name).isEqualTo("Tenant with ñáéíóú 中文 🎉")
        assertThat(domain.dbUsername).isEqualTo("user_ñ")
        assertThat(domain.dbPassword).isEqualTo("pässwörd")
    }

    @Test
    fun `should map ConfigEntity with special characters in tenantId`() {
        // Arrange
        val entity = ConfigEntity(
            id = UUID.randomUUID(),
            tenantId = "tenant-with.special:chars_123",
            primaryColor = "#ABCDEF",
            secondaryColor = null,
            logoUrl = "https://example.com/path?param=value"
        )

        // Act
        val domain = entity.toDomain()

        // Assert
        assertThat(domain.tenantId).isEqualTo("tenant-with.special:chars_123")
        assertThat(domain.logoUrl).isEqualTo("https://example.com/path?param=value")
    }

    @Test
    fun `should handle very old timestamps`() {
        // Arrange
        val oldDate = ZonedDateTime.parse("2000-01-01T00:00:00Z")
        val entity = TenantEntity(
            id = "tenant-old",
            name = "Old Tenant",
            dbDatabase = "old_db",
            dbUsername = "old_user",
            dbPassword = "old_pass",
            isActive = true,
            createdAt = oldDate,
            updatedAt = oldDate
        )

        // Act
        val domain = entity.toDomain()

        // Assert
        assertThat(domain.createdAt).isEqualTo(oldDate)
        assertThat(domain.updatedAt).isEqualTo(oldDate)
    }

    @Test
    fun `should handle future timestamps`() {
        // Arrange
        val futureDate = ZonedDateTime.parse("2030-12-31T23:59:59Z")
        val entity = TenantEntity(
            id = "tenant-future",
            name = "Future Tenant",
            dbDatabase = "future_db",
            dbUsername = "future_user",
            dbPassword = "future_pass",
            isActive = true,
            createdAt = futureDate,
            updatedAt = futureDate
        )

        // Act
        val domain = entity.toDomain()

        // Assert
        assertThat(domain.createdAt).isEqualTo(futureDate)
        assertThat(domain.updatedAt).isEqualTo(futureDate)
    }

    @Test
    fun `should convert valid hex colors in ConfigEntity`() {
        // Arrange
        val entity = ConfigEntity(
            id = UUID.randomUUID(),
            tenantId = "tenant-colors",
            primaryColor = "#FFFFFF",
            secondaryColor = "#000000",
            logoUrl = null
        )

        // Act
        val domain = entity.toDomain()

        // Assert
        assertThat(domain.primaryColor).isEqualTo("#FFFFFF")
        assertThat(domain.secondaryColor).isEqualTo("#000000")
    }

    @Test
    fun `should handle invalid color formats gracefully`() {
        // Arrange
        val entity = ConfigEntity(
            id = UUID.randomUUID(),
            tenantId = "tenant-invalid-colors",
            primaryColor = "not-a-color",
            secondaryColor = "123",
            logoUrl = null
        )

        // Act
        val domain = entity.toDomain()

        // Assert - Mapper should pass through values without validation
        assertThat(domain.primaryColor).isEqualTo("not-a-color")
        assertThat(domain.secondaryColor).isEqualTo("123")
    }
}
