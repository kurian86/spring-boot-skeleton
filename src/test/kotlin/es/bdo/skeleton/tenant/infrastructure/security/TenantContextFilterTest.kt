package es.bdo.skeleton.tenant.infrastructure.security

import es.bdo.skeleton.tenant.domain.Tenant
import es.bdo.skeleton.tenant.domain.TenantRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*

class TenantContextFilterTest {

    private lateinit var filter: TenantContextFilter
    private lateinit var repository: TenantRepository
    private lateinit var request: HttpServletRequest
    private lateinit var response: HttpServletResponse
    private lateinit var filterChain: FilterChain

    @BeforeEach
    fun setUp() {
        repository = mock()
        filter = TenantContextFilter(repository)
        request = mock()
        response = mock()
        filterChain = mock()
    }

    @Test
    fun `should allow request with valid tenant header`() {
        // Given
        whenever(request.getHeader("X-Tenant-ID")).thenReturn("default")
        whenever(repository.findById("default")).thenReturn(createTenant("default", true))

        // When
        filter.doFilter(request, response, filterChain)

        // Then
        verify(filterChain).doFilter(request, response)
    }

    @Test
    fun `should use default tenant when no header`() {
        // Given
        whenever(request.getHeader("X-Tenant-ID")).thenReturn(null)
        whenever(repository.findById("default")).thenReturn(createTenant("default", true))

        // When
        filter.doFilter(request, response, filterChain)

        // Then
        verify(filterChain).doFilter(request, response)
    }

    @Test
    fun `should throw exception when tenant not found`() {
        // Given
        whenever(request.getHeader("X-Tenant-ID")).thenReturn("unknown")
        whenever(repository.findById("unknown")).thenReturn(null)

        // When/Then
        assertThrows<es.bdo.skeleton.tenant.application.exception.TenantNotFoundException> {
            filter.doFilter(request, response, filterChain)
        }
    }

    @Test
    fun `should throw exception when tenant is inactive`() {
        // Given
        whenever(request.getHeader("X-Tenant-ID")).thenReturn("inactive")
        whenever(repository.findById("inactive")).thenReturn(createTenant("inactive", false))

        // When/Then
        assertThrows<es.bdo.skeleton.tenant.application.exception.TenantNotFoundException> {
            filter.doFilter(request, response, filterChain)
        }
        verify(repository).evictCache("inactive")
    }

    private fun createTenant(id: String, active: Boolean): Tenant {
        return Tenant(
            id = id,
            name = id,
            dbDatabase = "test",
            dbUsername = "test",
            dbPassword = "test",
            isActive = active,
            createdAt = java.time.ZonedDateTime.now(),
            updatedAt = java.time.ZonedDateTime.now()
        )
    }
}
