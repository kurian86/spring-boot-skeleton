package es.bdo.skeleton.user.infrastructure

import es.bdo.skeleton.shared.model.Filter
import es.bdo.skeleton.shared.model.FilterGroup
import es.bdo.skeleton.shared.model.Operator
import es.bdo.skeleton.user.domain.UserStatus
import es.bdo.skeleton.user.infrastructure.model.UserEntity
import es.bdo.skeleton.user.infrastructure.specification.UserFilterSpecification
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
import java.util.UUID
import javax.sql.DataSource

@DataJpaTest(
    properties = [
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
    ]
)
@AutoConfigureTestDatabase
@ContextConfiguration(classes = [UserRepositorySliceTest.TestConfig::class])
class UserRepositorySliceTest {

    @Configuration
    @EnableTransactionManagement
    @EnableJpaRepositories(
        basePackageClasses = [UserJpaRepository::class],
        entityManagerFactoryRef = "tenantEntityManagerFactory",
        transactionManagerRef = "tenantTransactionManager"
    )
    class TestConfig {

        @Bean
        fun tenantEntityManagerFactory(
            builder: EntityManagerFactoryBuilder,
            dataSource: DataSource
        ): LocalContainerEntityManagerFactoryBean =
            builder
                .dataSource(dataSource)
                .packages("es.bdo.skeleton.user.infrastructure.model")
                .persistenceUnit("tenant")
                .build()

        @Bean
        fun tenantTransactionManager(
            tenantEntityManagerFactory: EntityManagerFactory
        ): PlatformTransactionManager =
            JpaTransactionManager(tenantEntityManagerFactory)

        @Bean
        fun userFilterSpecification() = UserFilterSpecification()

        @Bean
        fun userRepository(
            jpaRepository: UserJpaRepository,
            filterSpecification: UserFilterSpecification
        ): es.bdo.skeleton.user.domain.UserRepository =
            UserRepository(jpaRepository, filterSpecification)
    }

    @Autowired
    private lateinit var jpaRepository: UserJpaRepository

    @Autowired
    private lateinit var userRepository: es.bdo.skeleton.user.domain.UserRepository

    @BeforeEach
    fun setUp() {
        jpaRepository.deleteAll()
    }

    private fun entity(
        email: String = "alice@example.com",
        status: UserStatus = UserStatus.ACTIVE,
        externalId: String? = null,
        name: String = "Alice",
    ) = UserEntity(
        id = UUID.randomUUID(),
        name = name,
        email = email,
        status = status,
        externalId = externalId,
        createdAt = ZonedDateTime.now(),
        updatedAt = ZonedDateTime.now(),
    )

    // --- count ---

    @Test
    fun `count returns zero when repository is empty`() {
        // Arrange

        // Act
        val count = jpaRepository.count()

        // Assert
        assertThat(count).isEqualTo(0L)
    }

    @Test
    fun `count returns correct number of saved users`() {
        // Arrange
        jpaRepository.saveAll(
            listOf(
                entity(email = "a@example.com"),
                entity(email = "b@example.com"),
                entity(email = "c@example.com"),
            )
        )

        // Act
        val count = jpaRepository.count()

        // Assert
        assertThat(count).isEqualTo(3L)
    }

    @Test
    fun `count reflects deletions`() {
        // Arrange
        val saved = jpaRepository.save(entity())
        jpaRepository.delete(saved)

        // Act
        val count = jpaRepository.count()

        // Assert
        assertThat(count).isEqualTo(0L)
    }

    // --- findAll ---

    @Test
    fun `findAll returns empty list when repository is empty`() {
        // Arrange

        // Act
        val result = jpaRepository.findAll().toList()

        // Assert
        assertThat(result).isEmpty()
    }

    @Test
    fun `findAll returns all saved users`() {
        // Arrange
        jpaRepository.saveAll(
            listOf(
                entity(email = "a@example.com"),
                entity(email = "b@example.com"),
            )
        )

        // Act
        val result = jpaRepository.findAll().toList()

        // Assert
        assertThat(result).hasSize(2)
    }

    @Test
    fun `findAll persists all fields correctly`() {
        // Arrange
        val original = entity(email = "full@example.com", status = UserStatus.DISABLED, externalId = "ext-99")
        jpaRepository.save(original)

        // Act
        val result = jpaRepository.findAll().toList().single()

        // Assert
        assertThat(result.id).isEqualTo(original.id)
        assertThat(result.name).isEqualTo("Alice")
        assertThat(result.email).isEqualTo("full@example.com")
        assertThat(result.status).isEqualTo(UserStatus.DISABLED)
        assertThat(result.externalId).isEqualTo("ext-99")
    }

    @Test
    fun `findAll persists null externalId correctly`() {
        // Arrange
        jpaRepository.save(entity(externalId = null))

        // Act
        val result = jpaRepository.findAll().toList().single()

        // Assert
        assertThat(result.externalId).isNull()
    }

    @Test
    fun `findAll preserves UserStatus enum as string`() {
        // Arrange
        jpaRepository.save(entity(status = UserStatus.DISABLED))

        // Act
        val result = jpaRepository.findAll().toList().single()

        // Assert
        assertThat(result.status).isEqualTo(UserStatus.DISABLED)
    }

    // --- findByEmail ---

    @Test
    fun `findByEmail returns user when email matches`() {
        // Arrange
        jpaRepository.save(entity(email = "find@example.com"))

        // Act
        val result = jpaRepository.findByEmail("find@example.com")

        // Assert
        assertThat(result).isPresent
        assertThat(result.get().email).isEqualTo("find@example.com")
    }

    @Test
    fun `findByEmail returns empty Optional when email not found`() {
        // Arrange

        // Act
        val result = jpaRepository.findByEmail("missing@example.com")

        // Assert
        assertThat(result).isEmpty
    }

    @Test
    fun `findByEmail is case-sensitive`() {
        // Arrange
        jpaRepository.save(entity(email = "case@example.com"))

        // Act
        val result = jpaRepository.findByEmail("CASE@example.com")

        // Assert
        assertThat(result).isEmpty
    }

    // --- findAll with filters ---

    @Test
    fun `findAll with EQUALITY filter on status should return filtered users`() {
        // Arrange
        val user1 = entity(email = "active@example.com", status = UserStatus.ACTIVE)
        val user2 = entity(email = "disabled@example.com", status = UserStatus.DISABLED)
        jpaRepository.save(user1)
        jpaRepository.save(user2)

        val filters = listOf(
            FilterGroup(
                filters = listOf(
                    Filter(property = "status", value = "ACTIVE", operator = Operator.EQUALITY)
                )
            )
        )

        // Act
        val (total, items) = userRepository.findAll(0, 10, null, filters)

        // Assert
        assertThat(total).isEqualTo(1L)
        assertThat(items).hasSize(1)
        assertThat(items[0].status).isEqualTo(UserStatus.ACTIVE)
    }

    @Test
    fun `findAll with LIKE filter on name should return matching users`() {
        // Arrange
        val user1 = entity(email = "john@example.com", name = "John Doe")
        val user2 = entity(email = "jane@example.com", name = "Jane Smith")
        jpaRepository.save(user1)
        jpaRepository.save(user2)

        val filters = listOf(
            FilterGroup(
                filters = listOf(
                    Filter(property = "name", value = "John", operator = Operator.LIKE)
                )
            )
        )

        // Act
        val (total, items) = userRepository.findAll(0, 10, null, filters)

        // Assert
        assertThat(total).isEqualTo(1L)
        assertThat(items).hasSize(1)
        assertThat(items[0].name).isEqualTo("John Doe")
    }

    @Test
    fun `findAll totalCount reflects all matching records across pages`() {
        // Arrange
        jpaRepository.save(entity(email = "active1@example.com", status = UserStatus.ACTIVE))
        jpaRepository.save(entity(email = "active2@example.com", status = UserStatus.ACTIVE))
        jpaRepository.save(entity(email = "disabled@example.com", status = UserStatus.DISABLED))

        val filters = listOf(
            FilterGroup(
                filters = listOf(
                    Filter(property = "status", value = "ACTIVE", operator = Operator.EQUALITY)
                )
            )
        )

        // Act
        val (total, _) = userRepository.findAll(0, 10, null, filters)

        // Assert
        assertThat(total).isEqualTo(2L)
    }
}
