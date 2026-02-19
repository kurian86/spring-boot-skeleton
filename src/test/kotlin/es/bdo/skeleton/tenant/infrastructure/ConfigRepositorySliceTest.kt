package es.bdo.skeleton.tenant.infrastructure

import es.bdo.skeleton.tenant.infrastructure.model.ConfigEntity
import jakarta.persistence.EntityManagerFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jpa.EntityManagerFactoryBuilder
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import java.util.UUID
import javax.sql.DataSource

@DataJpaTest(
    properties = [
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
    ]
)
@AutoConfigureTestDatabase
@ContextConfiguration(classes = [ConfigRepositorySliceTest.TestConfig::class])
class ConfigRepositorySliceTest {

    @Configuration
    @EnableTransactionManagement
    @EnableJpaRepositories(
        basePackageClasses = [ConfigJpaRepository::class],
        entityManagerFactoryRef = "catalogEntityManagerFactory",
        transactionManagerRef = "catalogTransactionManager"
    )
    class TestConfig {

        @Bean
        fun catalogEntityManagerFactory(
            builder: EntityManagerFactoryBuilder,
            dataSource: DataSource
        ): LocalContainerEntityManagerFactoryBean =
            builder
                .dataSource(dataSource)
                .packages("es.bdo.skeleton.tenant.infrastructure.model")
                .persistenceUnit("catalog")
                .build()

        @Bean
        fun catalogTransactionManager(
            catalogEntityManagerFactory: EntityManagerFactory
        ): PlatformTransactionManager =
            JpaTransactionManager(catalogEntityManagerFactory)
    }

    @Autowired
    private lateinit var repository: ConfigJpaRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun setUp() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE")
        repository.deleteAll()
    }

    private fun configEntity(
        tenantId: String,
        primaryColor: String? = null,
        secondaryColor: String? = null,
        logoUrl: String? = null
    ) = ConfigEntity(
        id = UUID.randomUUID(),
        tenantId = tenantId,
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        logoUrl = logoUrl
    )

    // --- findByTenantId ---

    @Test
    fun `findByTenantId returns config when tenantId matches`() {
        // Arrange
        repository.save(configEntity("acme", primaryColor = "#FF0000"))

        // Act
        val result = repository.findByTenantId("acme")

        // Assert
        assertThat(result).isPresent
        assertThat(result.get().tenantId).isEqualTo("acme")
        assertThat(result.get().primaryColor).isEqualTo("#FF0000")
    }

    @Test
    fun `findByTenantId returns empty for unknown tenantId`() {
        // Arrange

        // Act
        val result = repository.findByTenantId("does-not-exist")

        // Assert
        assertThat(result).isEmpty
    }

    @Test
    fun `findByTenantId returns correct config when multiple configs exist`() {
        // Arrange
        repository.saveAll(
            listOf(
                configEntity("tenant-a", primaryColor = "#AAAAAA"),
                configEntity("tenant-b", primaryColor = "#BBBBBB")
            )
        )

        // Act
        val result = repository.findByTenantId("tenant-b")

        // Assert
        assertThat(result).isPresent
        assertThat(result.get().tenantId).isEqualTo("tenant-b")
        assertThat(result.get().primaryColor).isEqualTo("#BBBBBB")
    }

    @Test
    fun `findByTenantId persists null optional fields correctly`() {
        // Arrange
        repository.save(configEntity("sparse"))

        // Act
        val result = repository.findByTenantId("sparse")

        // Assert
        assertThat(result).isPresent
        assertThat(result.get().primaryColor).isNull()
        assertThat(result.get().secondaryColor).isNull()
        assertThat(result.get().logoUrl).isNull()
    }

    @Test
    fun `findByTenantId returns all optional fields when all are set`() {
        // Arrange
        repository.save(
            configEntity(
                "full",
                primaryColor = "#112233",
                secondaryColor = "#445566",
                logoUrl = "https://example.com/logo.png"
            )
        )

        // Act
        val result = repository.findByTenantId("full")

        // Assert
        assertThat(result).isPresent
        val config = result.get()
        assertThat(config.primaryColor).isEqualTo("#112233")
        assertThat(config.secondaryColor).isEqualTo("#445566")
        assertThat(config.logoUrl).isEqualTo("https://example.com/logo.png")
    }

    @Test
    fun `findByTenantId preserves UUID identity across persist and retrieve`() {
        // Arrange
        val expectedId = UUID.randomUUID()
        repository.save(
            ConfigEntity(
                id = expectedId,
                tenantId = "uuid-check",
                primaryColor = "#000000"
            )
        )

        // Act
        val result = repository.findByTenantId("uuid-check")

        // Assert
        assertThat(result).isPresent
        assertThat(result.get().id).isEqualTo(expectedId)
    }
}
