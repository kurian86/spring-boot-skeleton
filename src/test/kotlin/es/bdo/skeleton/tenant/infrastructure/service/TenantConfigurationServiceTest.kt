package es.bdo.skeleton.tenant.infrastructure.service

import es.bdo.skeleton.tenant.application.exception.TenantNotFoundException
import es.bdo.skeleton.tenant.domain.Tenant
import es.bdo.skeleton.tenant.domain.TenantRepository
import es.bdo.skeleton.tenant.infrastructure.config.TenantProperties
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.time.ZonedDateTime

class TenantConfigurationServiceTest {

    @Mock
    private lateinit var encryptionService: EncryptionService

    @Mock
    private lateinit var properties: TenantProperties

    @Mock
    private lateinit var repository: TenantRepository

    @Mock
    private lateinit var datasourceProperties: TenantProperties.Datasource

    private lateinit var service: TenantConfigurationService

    private val testTenant = Tenant(
        id = "tenant-1",
        name = "Test Tenant",
        dbDatabase = "test_db",
        dbUsername = "test_user",
        dbPassword = "encrypted_password",
        isActive = true,
        createdAt = ZonedDateTime.now(),
        updatedAt = ZonedDateTime.now()
    )

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        setupProperties()
        `when`(encryptionService.decrypt("encrypted_password")).thenReturn("decrypted_password")
    }

    private fun setupProperties() {
        `when`(properties.datasource).thenReturn(datasourceProperties)
        `when`(datasourceProperties.urlTemplate).thenReturn("jdbc:postgresql://localhost:5432/%s")
        `when`(datasourceProperties.driverClassName).thenReturn("org.postgresql.Driver")
        `when`(datasourceProperties.maximumPoolSize).thenReturn(10)
        `when`(datasourceProperties.minimumIdle).thenReturn(5)
        `when`(datasourceProperties.maxLifetime).thenReturn(300000L)
        `when`(datasourceProperties.idleTimeout).thenReturn(120000L)
        `when`(datasourceProperties.connectionTimeout).thenReturn(30000L)
        `when`(datasourceProperties.keepaliveTime).thenReturn(60000L)
    }

    @Test
    fun `should load all active tenants on initialization`() {
        // Arrange
        val tenants = listOf(testTenant)
        `when`(repository.findAllActive()).thenReturn(tenants)

        // Act
        service = TenantConfigurationService(encryptionService, properties, repository)

        // Assert
        verify(repository).findAllActive()
        val dataSources = service.getAllActiveDataSources()
        assertThat(dataSources).hasSize(1)
        assertThat(dataSources).containsKey("tenant-1")
    }

    @Test
    fun `should return existing datasource when tenant already loaded`() {
        // Arrange
        `when`(repository.findAllActive()).thenReturn(listOf(testTenant))
        service = TenantConfigurationService(encryptionService, properties, repository)

        // Act
        val dataSource1 = service.getOrCreateDataSource("tenant-1")
        val dataSource2 = service.getOrCreateDataSource("tenant-1")

        // Assert
        assertThat(dataSource1).isSameAs(dataSource2)
        // findById should not be called when datasource is already cached
        verify(repository, never()).findById("tenant-1")
    }

    @Test
    fun `should create new datasource when tenant not in cache`() {
        // Arrange
        `when`(repository.findAllActive()).thenReturn(emptyList())
        `when`(repository.findById("tenant-1")).thenReturn(testTenant)
        service = TenantConfigurationService(encryptionService, properties, repository)

        // Act
        val dataSource = service.getOrCreateDataSource("tenant-1")

        // Assert
        assertThat(dataSource).isNotNull
        verify(repository).findById("tenant-1")
    }

    @Test
    fun `should throw exception when tenant not found`() {
        // Arrange
        `when`(repository.findAllActive()).thenReturn(emptyList())
        `when`(repository.findById("unknown-tenant")).thenReturn(null)
        service = TenantConfigurationService(encryptionService, properties, repository)

        // Act & Assert
        assertThatThrownBy {
            service.getOrCreateDataSource("unknown-tenant")
        }.isInstanceOf(TenantNotFoundException::class.java)
            .hasMessageContaining("Tenant not found: unknown-tenant")
    }

    @Test
    fun `should throw exception when tenant is inactive`() {
        // Arrange
        val inactiveTenant = testTenant.copy(isActive = false)
        `when`(repository.findAllActive()).thenReturn(emptyList())
        `when`(repository.findById("tenant-1")).thenReturn(inactiveTenant)
        service = TenantConfigurationService(encryptionService, properties, repository)

        // Act & Assert
        assertThatThrownBy {
            service.getOrCreateDataSource("tenant-1")
        }.isInstanceOf(TenantNotFoundException::class.java)
            .hasMessageContaining("Tenant is not active: tenant-1")
    }

    @Test
    fun `should evict datasource and cache`() {
        // Arrange
        `when`(repository.findAllActive()).thenReturn(listOf(testTenant))
        service = TenantConfigurationService(encryptionService, properties, repository)

        // Act
        service.evictDataSource("tenant-1")

        // Assert
        verify(repository).evictCache("tenant-1")
        assertThat(service.getAllActiveDataSources()).doesNotContainKey("tenant-1")
    }

    @Test
    fun `should add new datasource`() {
        // Arrange
        `when`(repository.findAllActive()).thenReturn(emptyList())
        service = TenantConfigurationService(encryptionService, properties, repository)

        // Act
        val dataSource = service.addDataSource(testTenant)

        // Assert
        assertThat(dataSource).isNotNull
        assertThat(service.getAllActiveDataSources()).containsKey("tenant-1")
    }

    @Test
    fun `should decrypt password when creating datasource`() {
        // Arrange
        `when`(repository.findAllActive()).thenReturn(listOf(testTenant))

        // Act
        service = TenantConfigurationService(encryptionService, properties, repository)

        // Assert
        verify(encryptionService).decrypt("encrypted_password")
    }

    @Test
    fun `should reload all tenants`() {
        // Arrange
        `when`(repository.findAllActive()).thenReturn(listOf(testTenant))
        service = TenantConfigurationService(encryptionService, properties, repository)

        val newTenant = testTenant.copy(id = "tenant-2")
        `when`(repository.findAllActive()).thenReturn(listOf(testTenant, newTenant))

        // Act
        val result = service.loadAllTenants()

        // Assert
        assertThat(result).hasSize(2)
        assertThat(result).containsKeys("tenant-1", "tenant-2")
    }

    @Test
    fun `should return copy of active datasources map`() {
        // Arrange
        `when`(repository.findAllActive()).thenReturn(listOf(testTenant))
        service = TenantConfigurationService(encryptionService, properties, repository)

        // Act
        val map1 = service.getAllActiveDataSources()
        val map2 = service.getAllActiveDataSources()

        // Assert
        assertThat(map1).isEqualTo(map2)
        assertThat(map1).isNotSameAs(map2)
    }
}
