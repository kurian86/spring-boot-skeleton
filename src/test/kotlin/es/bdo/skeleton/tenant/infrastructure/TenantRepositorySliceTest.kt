package es.bdo.skeleton.tenant.infrastructure

import es.bdo.skeleton.tenant.infrastructure.model.TenantEntity
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
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import java.time.ZonedDateTime
import javax.sql.DataSource

@DataJpaTest(
    properties = [
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
    ]
)
@AutoConfigureTestDatabase
@ContextConfiguration(classes = [TenantRepositorySliceTest.TestConfig::class])
class TenantRepositorySliceTest {

    @Configuration
    @EnableTransactionManagement
    @EnableJpaRepositories(
        basePackageClasses = [TenantJpaRepository::class],
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
    private lateinit var repository: TenantJpaRepository

    private val now = ZonedDateTime.now()

    private fun tenant(id: String, active: Boolean) = TenantEntity(
        id = id,
        name = "Tenant $id",
        dbDatabase = "db_$id",
        dbUsername = "user_$id",
        dbPassword = "pass_$id",
        isActive = active,
        createdAt = now,
        updatedAt = now
    )

    @BeforeEach
    fun setUp() {
        repository.deleteAll()
    }

    // --- findAllByIsActive ---

    @Test
    fun `findAllByIsActive returns only active tenants when querying active`() {
        // Arrange
        repository.saveAll(
            listOf(
                tenant("alpha", active = true),
                tenant("beta", active = true),
                tenant("gamma", active = false)
            )
        )

        // Act
        val result = repository.findAllByIsActive(true)

        // Assert
        assertThat(result).hasSize(2)
        assertThat(result.map { it.id }).containsExactlyInAnyOrder("alpha", "beta")
    }

    @Test
    fun `findAllByIsActive returns only inactive tenants when querying inactive`() {
        // Arrange
        repository.saveAll(
            listOf(
                tenant("active-one", active = true),
                tenant("inactive-one", active = false),
                tenant("inactive-two", active = false)
            )
        )

        // Act
        val result = repository.findAllByIsActive(false)

        // Assert
        assertThat(result).hasSize(2)
        assertThat(result.map { it.id }).containsExactlyInAnyOrder("inactive-one", "inactive-two")
    }

    @Test
    fun `findAllByIsActive returns empty list when no tenants match`() {
        // Arrange
        repository.save(tenant("only-active", active = true))

        // Act
        val result = repository.findAllByIsActive(false)

        // Assert
        assertThat(result).isEmpty()
    }

    @Test
    fun `findAllByIsActive returns empty list when repository is empty`() {
        // Arrange

        // Act
        val result = repository.findAllByIsActive(true)

        // Assert
        assertThat(result).isEmpty()
    }

    @Test
    fun `findAllByIsActive returns all tenants when all are active`() {
        // Arrange
        repository.saveAll(
            listOf(
                tenant("t1", active = true),
                tenant("t2", active = true),
                tenant("t3", active = true)
            )
        )

        // Act
        val result = repository.findAllByIsActive(true)

        // Assert
        assertThat(result).hasSize(3)
    }

    @Test
    fun `findAllByIsActive returns correct entity fields`() {
        // Arrange
        val saved = tenant("field-check", active = true)
        repository.save(saved)

        // Act
        val result = repository.findAllByIsActive(true)

        // Assert
        assertThat(result).hasSize(1)
        val entity = result.first()
        assertThat(entity.id).isEqualTo("field-check")
        assertThat(entity.name).isEqualTo("Tenant field-check")
        assertThat(entity.dbDatabase).isEqualTo("db_field-check")
        assertThat(entity.dbUsername).isEqualTo("user_field-check")
        assertThat(entity.dbPassword).isEqualTo("pass_field-check")
        assertThat(entity.isActive).isTrue()
    }
}
