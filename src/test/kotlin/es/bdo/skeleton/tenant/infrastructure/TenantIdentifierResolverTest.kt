package es.bdo.skeleton.tenant.infrastructure

import es.bdo.skeleton.tenant.application.TenantContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TenantIdentifierResolverTest {

    private val resolver = TenantIdentifierResolver()

    @Test
    fun `should return default tenant when context is not bound`() {
        // Act
        val tenantId = resolver.resolveCurrentTenantIdentifier()

        // Assert
        assertThat(tenantId).isEqualTo(TenantContext.DEFAULT_TENANT)
    }

    @Test
    fun `should return tenant from context when bound`() {
        // Act
        val tenantId = TenantContext.withTenant("my-tenant") {
            resolver.resolveCurrentTenantIdentifier()
        }

        // Assert
        assertThat(tenantId).isEqualTo("my-tenant")
    }

    @Test
    fun `should always return true for validateExistingCurrentSessions`() {
        // Act & Assert
        assertThat(resolver.validateExistingCurrentSessions()).isTrue()
    }

    @Test
    fun `should handle different tenant ids correctly`() {
        // Arrange
        val tenantIds = listOf(
            "tenant-1",
            "tenant-2",
            "test-tenant",
            "prod-tenant",
            "a".repeat(100)
        )

        // Act & Assert
        tenantIds.forEach { tenantId ->
            val result = TenantContext.withTenant(tenantId) {
                resolver.resolveCurrentTenantIdentifier()
            }
            assertThat(result).isEqualTo(tenantId)
        }
    }

    @Test
    fun `should return default tenant after withTenant scope ends`() {
        // Act
        TenantContext.withTenant("temp-tenant") {
            assertThat(resolver.resolveCurrentTenantIdentifier()).isEqualTo("temp-tenant")
        }

        // Assert
        assertThat(resolver.resolveCurrentTenantIdentifier()).isEqualTo(TenantContext.DEFAULT_TENANT)
    }

    @Test
    fun `should handle concurrent tenant contexts`() {
        // Arrange
        val results = mutableListOf<String>()

        // Act
        TenantContext.withTenant("tenant-a") {
            results.add(resolver.resolveCurrentTenantIdentifier())
        }

        TenantContext.withTenant("tenant-b") {
            results.add(resolver.resolveCurrentTenantIdentifier())
        }

        results.add(resolver.resolveCurrentTenantIdentifier())

        // Assert
        assertThat(results).containsExactly("tenant-a", "tenant-b", TenantContext.DEFAULT_TENANT)
    }

    @Test
    fun `should implement CurrentTenantIdentifierResolver interface`() {
        // Assert
        assertThat(resolver).isInstanceOf(org.hibernate.context.spi.CurrentTenantIdentifierResolver::class.java)
    }
}
