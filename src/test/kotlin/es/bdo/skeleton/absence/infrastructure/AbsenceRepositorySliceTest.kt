package es.bdo.skeleton.absence.infrastructure

import es.bdo.skeleton.absence.infrastructure.model.AbsenceEntity
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
import java.time.LocalDate
import java.util.UUID
import javax.sql.DataSource

@DataJpaTest(
    properties = [
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
    ]
)
@AutoConfigureTestDatabase
@ContextConfiguration(classes = [AbsenceRepositorySliceTest.TestConfig::class])
class AbsenceRepositorySliceTest {

    @Configuration
    @EnableTransactionManagement
    @EnableJpaRepositories(
        basePackageClasses = [AbsenceJpaRepository::class],
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
                .packages("es.bdo.skeleton.absence.infrastructure.model")
                .persistenceUnit("tenant")
                .build()

        @Bean
        fun tenantTransactionManager(
            tenantEntityManagerFactory: EntityManagerFactory
        ): PlatformTransactionManager =
            JpaTransactionManager(tenantEntityManagerFactory)
    }

    @Autowired
    private lateinit var jpaRepository: AbsenceJpaRepository

    private val userId = UUID.randomUUID()

    private fun absence(
        startDate: LocalDate = LocalDate.of(2024, 1, 1),
        endDate: LocalDate? = null
    ) = AbsenceEntity(
        id = UUID.randomUUID(),
        userId = userId,
        startDate = startDate,
        endDate = endDate
    )

    @BeforeEach
    fun setUp() {
        jpaRepository.deleteAll()
    }

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
    fun `count returns correct number of saved absences`() {
        // Arrange
        jpaRepository.saveAll(listOf(absence(), absence(), absence()))

        // Act
        val count = jpaRepository.count()

        // Assert
        assertThat(count).isEqualTo(3L)
    }

    @Test
    fun `count reflects deletions`() {
        // Arrange
        val saved = jpaRepository.save(absence())
        jpaRepository.delete(saved)

        // Act
        val count = jpaRepository.count()

        // Assert
        assertThat(count).isEqualTo(0L)
    }

    // --- findAll ---

    @Test
    fun `findAll returns empty when repository is empty`() {
        // Arrange

        // Act
        val result = jpaRepository.findAll().toList()

        // Assert
        assertThat(result).isEmpty()
    }

    @Test
    fun `findAll returns all saved absences`() {
        // Arrange
        jpaRepository.saveAll(
            listOf(
                absence(startDate = LocalDate.of(2024, 1, 1)),
                absence(startDate = LocalDate.of(2024, 2, 1)),
                absence(startDate = LocalDate.of(2024, 3, 1))
            )
        )

        // Act
        val result = jpaRepository.findAll().toList()

        // Assert
        assertThat(result).hasSize(3)
    }

    @Test
    fun `findAll persists and retrieves all fields correctly`() {
        // Arrange
        val startDate = LocalDate.of(2024, 4, 10)
        val endDate = LocalDate.of(2024, 4, 15)
        val saved = jpaRepository.save(absence(startDate = startDate, endDate = endDate))

        // Act
        val result = jpaRepository.findAll().toList()

        // Assert
        assertThat(result).hasSize(1)
        val entity = result.single()
        assertThat(entity.id).isEqualTo(saved.id)
        assertThat(entity.userId).isEqualTo(userId)
        assertThat(entity.startDate).isEqualTo(startDate)
        assertThat(entity.endDate).isEqualTo(endDate)
    }

    @Test
    fun `findAll persists null endDate correctly`() {
        // Arrange
        jpaRepository.save(absence(endDate = null))

        // Act
        val result = jpaRepository.findAll().toList()

        // Assert
        assertThat(result.single().endDate).isNull()
    }

    @Test
    fun `findAll preserves UUID identity`() {
        // Arrange
        val expectedId = UUID.randomUUID()
        jpaRepository.save(AbsenceEntity(id = expectedId, userId = userId, startDate = LocalDate.of(2024, 5, 1)))

        // Act
        val result = jpaRepository.findAll().toList()

        // Assert
        assertThat(result.single().id).isEqualTo(expectedId)
    }
}
