package es.bdo.skeleton.tenant.infrastructure.security

import es.bdo.skeleton.tenant.application.TenantContext
import es.bdo.skeleton.tenant.application.exception.TenantNotFoundException
import es.bdo.skeleton.tenant.domain.Tenant
import es.bdo.skeleton.tenant.domain.TenantRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.time.ZonedDateTime

class TenantContextFilterTest {

    @Mock
    private lateinit var repository: TenantRepository

    @Mock
    private lateinit var request: HttpServletRequest

    @Mock
    private lateinit var response: HttpServletResponse

    @Mock
    private lateinit var filterChain: FilterChain

    private lateinit var filter: TenantContextFilter

    private val testTenant = Tenant(
        id = "tenant-1",
        name = "Test Tenant",
        dbDatabase = "test_db",
        dbUsername = "test_user",
        dbPassword = "encrypted_pass",
        isActive = true,
        createdAt = ZonedDateTime.now(),
        updatedAt = ZonedDateTime.now()
    )

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        filter = TenantContextFilter(repository)
    }

    @Test
    fun `should extract tenant from header and set context`() {
        // Arrange
        `when`(request.getHeader("X-Tenant-ID")).thenReturn("tenant-1")
        `when`(repository.findById("tenant-1")).thenReturn(testTenant)

        var capturedTenantId: String? = null
        doAnswer { invocation ->
            capturedTenantId = TenantContext.getOrNull()
        }.`when`(filterChain).doFilter(request, response)

        // Act
        filter.doFilter(request as ServletRequest, response as ServletResponse, filterChain)

        // Assert
        assertThat(capturedTenantId).isEqualTo("tenant-1")
        verify(filterChain).doFilter(request, response)
    }

    @Test
    fun `should use default tenant when header is missing`() {
        // Arrange
        `when`(request.getHeader("X-Tenant-ID")).thenReturn(null)
        `when`(repository.findById(TenantContext.DEFAULT_TENANT)).thenReturn(testTenant.copy(id = TenantContext.DEFAULT_TENANT))

        var capturedTenantId: String? = null
        doAnswer { invocation ->
            capturedTenantId = TenantContext.getOrNull()
        }.`when`(filterChain).doFilter(request, response)

        // Act
        filter.doFilter(request as ServletRequest, response as ServletResponse, filterChain)

        // Assert
        assertThat(capturedTenantId).isEqualTo(TenantContext.DEFAULT_TENANT)
    }

    @Test
    fun `should throw exception when tenant id is blank`() {
        // Arrange
        `when`(request.getHeader("X-Tenant-ID")).thenReturn("   ")

        // Act & Assert
        assertThatThrownBy {
            filter.doFilter(request as ServletRequest, response as ServletResponse, filterChain)
        }.isInstanceOf(TenantNotFoundException::class.java)
            .hasMessageContaining("Tenant ID cannot be blank")
    }

    @Test
    fun `should throw exception when tenant not found`() {
        // Arrange
        `when`(request.getHeader("X-Tenant-ID")).thenReturn("unknown-tenant")
        `when`(repository.findById("unknown-tenant")).thenReturn(null)

        // Act & Assert
        assertThatThrownBy {
            filter.doFilter(request as ServletRequest, response as ServletResponse, filterChain)
        }.isInstanceOf(TenantNotFoundException::class.java)
            .hasMessageContaining("Tenant not found: unknown-tenant")
    }

    @Test
    fun `should throw exception and evict cache when tenant is inactive`() {
        // Arrange
        val inactiveTenant = testTenant.copy(id = "inactive-tenant", isActive = false)
        `when`(request.getHeader("X-Tenant-ID")).thenReturn("inactive-tenant")
        `when`(repository.findById("inactive-tenant")).thenReturn(inactiveTenant)

        // Act & Assert
        assertThatThrownBy {
            filter.doFilter(request as ServletRequest, response as ServletResponse, filterChain)
        }.isInstanceOf(TenantNotFoundException::class.java)
            .hasMessageContaining("Tenant is not active: inactive-tenant")

        verify(repository).evictCache("inactive-tenant")
    }

    @Test
    fun `should continue filter chain within tenant context`() {
        // Arrange
        `when`(request.getHeader("X-Tenant-ID")).thenReturn("tenant-1")
        `when`(repository.findById("tenant-1")).thenReturn(testTenant)

        var filterChainExecuted = false
        var tenantInFilterChain: String? = null

        doAnswer { invocation ->
            filterChainExecuted = true
            tenantInFilterChain = TenantContext.getOrNull()
        }.`when`(filterChain).doFilter(request, response)

        // Act
        filter.doFilter(request as ServletRequest, response as ServletResponse, filterChain)

        // Assert
        assertThat(filterChainExecuted).isTrue()
        assertThat(tenantInFilterChain).isEqualTo("tenant-1")
    }

    @Test
    fun `should handle different tenant ids correctly`() {
        // Arrange
        val tenantIds = listOf("tenant-a", "tenant-b", "tenant-c")

        tenantIds.forEach { tenantId ->
            `when`(request.getHeader("X-Tenant-ID")).thenReturn(tenantId)
            `when`(repository.findById(tenantId)).thenReturn(testTenant.copy(id = tenantId))

            var capturedId: String? = null
            doAnswer { _ ->
                capturedId = TenantContext.getOrNull()
            }.`when`(filterChain).doFilter(request, response)

            // Act
            filter.doFilter(request as ServletRequest, response as ServletResponse, filterChain)

            // Assert
            assertThat(capturedId).isEqualTo(tenantId)
        }
    }

    @Test
    fun `should validate tenant is active before setting context`() {
        // Arrange
        `when`(request.getHeader("X-Tenant-ID")).thenReturn("tenant-1")
        `when`(repository.findById("tenant-1")).thenReturn(testTenant)

        var filterExecuted = false
        doAnswer { _ ->
            filterExecuted = true
        }.`when`(filterChain).doFilter(request, response)

        // Act
        filter.doFilter(request as ServletRequest, response as ServletResponse, filterChain)

        // Assert
        assertThat(filterExecuted).isTrue()
        verify(repository).findById("tenant-1")
    }

    @Test
    fun `should restore default tenant after filter completes`() {
        // Arrange
        `when`(request.getHeader("X-Tenant-ID")).thenReturn("tenant-1")
        `when`(repository.findById("tenant-1")).thenReturn(testTenant)

        doAnswer { _ ->
            // Inside filter chain - tenant should be set
            assertThat(TenantContext.getOrNull()).isEqualTo("tenant-1")
        }.`when`(filterChain).doFilter(request, response)

        // Act
        filter.doFilter(request as ServletRequest, response as ServletResponse, filterChain)

        // Assert - After filter completes, tenant should be restored to default
        assertThat(TenantContext.tenantId).isEqualTo(TenantContext.DEFAULT_TENANT)
    }
}
