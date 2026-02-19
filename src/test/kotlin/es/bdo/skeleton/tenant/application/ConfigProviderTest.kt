package es.bdo.skeleton.tenant.application

import es.bdo.skeleton.tenant.application.model.ConfigDTO
import es.bdo.skeleton.tenant.domain.Config
import es.bdo.skeleton.tenant.domain.ConfigRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.util.*

class ConfigProviderTest {

    @Mock
    private lateinit var repository: ConfigRepository

    private var configProvider: ConfigProvider

    private val testConfig = Config(
        id = UUID.randomUUID(),
        tenantId = "tenant-1",
        primaryColor = "#FF0000",
        secondaryColor = "#00FF00",
        logoUrl = "https://example.com/logo.png"
    )

    init {
        MockitoAnnotations.openMocks(this)
        configProvider = ConfigProvider(repository)
    }

    @AfterEach
    fun tearDown() {
        // Clean up any bound tenant context
    }

    @Test
    fun `should return config DTO when tenant context is bound`() {
        // Arrange
        `when`(repository.findByTenantId("tenant-1")).thenReturn(testConfig)

        // Act
        val result = TenantContext.withTenant("tenant-1") {
            configProvider.find()
        }

        // Assert
        assertThat(result).isNotNull
        assertThat(result?.tenantId).isEqualTo("tenant-1")
        assertThat(result?.primaryColor).isEqualTo("#FF0000")
        assertThat(result?.secondaryColor).isEqualTo("#00FF00")
        assertThat(result?.logoUrl).isEqualTo("https://example.com/logo.png")
    }

    @Test
    fun `should return null when tenant context is not bound`() {
        // Act
        val result = configProvider.find()

        // Assert
        assertThat(result).isNull()
        verifyNoInteractions(repository)
    }

    @Test
    fun `should return null when config not found for tenant`() {
        // Arrange
        `when`(repository.findByTenantId("tenant-1")).thenReturn(null)

        // Act
        val result = TenantContext.withTenant("tenant-1") {
            configProvider.find()
        }

        // Assert
        assertThat(result).isNull()
        verify(repository).findByTenantId("tenant-1")
    }

    @Test
    fun `should handle config with null optional fields`() {
        // Arrange
        val configWithNulls = testConfig.copy(
            primaryColor = null,
            secondaryColor = null,
            logoUrl = null
        )
        `when`(repository.findByTenantId("tenant-1")).thenReturn(configWithNulls)

        // Act
        val result = TenantContext.withTenant("tenant-1") {
            configProvider.find()
        }

        // Assert
        assertThat(result).isNotNull
        assertThat(result?.primaryColor).isNull()
        assertThat(result?.secondaryColor).isNull()
        assertThat(result?.logoUrl).isNull()
    }

    @Test
    fun `should map different tenants to different configs`() {
        // Arrange
        val configTenant2 = testConfig.copy(
            tenantId = "tenant-2",
            primaryColor = "#0000FF"
        )
        `when`(repository.findByTenantId("tenant-1")).thenReturn(testConfig)
        `when`(repository.findByTenantId("tenant-2")).thenReturn(configTenant2)

        // Act
        val result1 = TenantContext.withTenant("tenant-1") {
            configProvider.find()
        }
        val result2 = TenantContext.withTenant("tenant-2") {
            configProvider.find()
        }

        // Assert
        assertThat(result1?.tenantId).isEqualTo("tenant-1")
        assertThat(result1?.primaryColor).isEqualTo("#FF0000")
        assertThat(result2?.tenantId).isEqualTo("tenant-2")
        assertThat(result2?.primaryColor).isEqualTo("#0000FF")
    }

    @Test
    fun `should return correct DTO mapping`() {
        // Arrange
        `when`(repository.findByTenantId("tenant-1")).thenReturn(testConfig)

        // Act
        val result = TenantContext.withTenant("tenant-1") {
            configProvider.find()
        }

        // Assert
        assertThat(result).isInstanceOf(ConfigDTO::class.java)
        assertThat(result?.tenantId).isEqualTo(testConfig.tenantId)
        assertThat(result?.primaryColor).isEqualTo(testConfig.primaryColor)
        assertThat(result?.secondaryColor).isEqualTo(testConfig.secondaryColor)
        assertThat(result?.logoUrl).isEqualTo(testConfig.logoUrl)
    }
}
