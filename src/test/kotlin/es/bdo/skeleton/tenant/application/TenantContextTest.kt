package es.bdo.skeleton.tenant.application

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TenantContextTest {

    @Test
    fun `should return default tenant when context is not bound`() {
        // Act
        val tenantId = TenantContext.tenantId

        // Assert
        assertThat(tenantId).isEqualTo(TenantContext.DEFAULT_TENANT)
    }

    @Test
    fun `should return null when context is not bound and getOrNull called`() {
        // Act
        val tenantId = TenantContext.getOrNull()

        // Assert
        assertThat(tenantId).isNull()
    }

    @Test
    fun `should return false for isBound when context is not bound`() {
        // Act & Assert
        assertThat(TenantContext.isBound()).isFalse()
    }

    @Test
    fun `should set and return tenant within withTenant scope`() {
        // Act
        val result = TenantContext.withTenant("tenant-abc") {
            TenantContext.tenantId
        }

        // Assert
        assertThat(result).isEqualTo("tenant-abc")
    }

    @Test
    fun `should return true for isBound within withTenant scope`() {
        // Act
        val result = TenantContext.withTenant("tenant-abc") {
            TenantContext.isBound()
        }

        // Assert
        assertThat(result).isTrue()
    }

    @Test
    fun `should return tenant from getOrNull within withTenant scope`() {
        // Act
        val result = TenantContext.withTenant("tenant-xyz") {
            TenantContext.getOrNull()
        }

        // Assert
        assertThat(result).isEqualTo("tenant-xyz")
    }

    @Test
    fun `should restore default tenant after withTenant scope ends`() {
        // Act
        TenantContext.withTenant("tenant-temp") {
            assertThat(TenantContext.tenantId).isEqualTo("tenant-temp")
        }

        // Assert
        assertThat(TenantContext.tenantId).isEqualTo(TenantContext.DEFAULT_TENANT)
    }

    @Test
    fun `should handle nested withTenant calls`() {
        // Act
        val result = TenantContext.withTenant("outer-tenant") {
            val outer = TenantContext.tenantId
            val inner = TenantContext.withTenant("inner-tenant") {
                TenantContext.tenantId
            }
            Pair(outer, inner)
        }

        // Assert
        assertThat(result.first).isEqualTo("outer-tenant")
        assertThat(result.second).isEqualTo("inner-tenant")
    }

    @Test
    fun `should isolate tenant contexts in separate withTenant calls`() {
        // Act
        val tenant1 = TenantContext.withTenant("tenant-1") {
            TenantContext.tenantId
        }
        val tenant2 = TenantContext.withTenant("tenant-2") {
            TenantContext.tenantId
        }

        // Assert
        assertThat(tenant1).isEqualTo("tenant-1")
        assertThat(tenant2).isEqualTo("tenant-2")
        assertThat(TenantContext.tenantId).isEqualTo(TenantContext.DEFAULT_TENANT)
    }

    @Test
    fun `should handle empty string as tenant id`() {
        // Act
        val result = TenantContext.withTenant("") {
            TenantContext.tenantId
        }

        // Assert
        assertThat(result).isEmpty()
    }

    @Test
    fun `should handle special characters in tenant id`() {
        // Arrange
        val specialTenantId = "tenant-123_test.domain:8080"

        // Act
        val result = TenantContext.withTenant(specialTenantId) {
            TenantContext.tenantId
        }

        // Assert
        assertThat(result).isEqualTo(specialTenantId)
    }

    @Test
    fun `should return value from operation`() {
        // Act
        val result = TenantContext.withTenant("tenant-1") {
            "return-value"
        }

        // Assert
        assertThat(result).isEqualTo("return-value")
    }

    @Test
    fun `should handle exceptions within withTenant scope`() {
        // Arrange
        val exception = RuntimeException("Test exception")

        // Act & Assert
        try {
            TenantContext.withTenant("tenant-1") {
                throw exception
            }
        } catch (e: RuntimeException) {
            assertThat(e).isSameAs(exception)
        }

        // Verify context is cleaned up after exception
        assertThat(TenantContext.tenantId).isEqualTo(TenantContext.DEFAULT_TENANT)
    }

    @Test
    fun `should execute complex operation within withTenant scope`() {
        // Act
        val result = TenantContext.withTenant("tenant-1") {
            val values = mutableListOf<String>()
            values.add(TenantContext.tenantId)
            values.add(TenantContext.getOrNull() ?: "null")
            values.add(if (TenantContext.isBound()) "bound" else "not-bound")
            values
        }

        // Assert
        assertThat(result).containsExactly("tenant-1", "tenant-1", "bound")
    }
}
