package es.bdo.skeleton.absence.infrastructure.model

import es.bdo.skeleton.absence.domain.Absence
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class AbsenceMapperTest {

    private val id = UUID.randomUUID()
    private val userId = UUID.randomUUID()
    private val startDate = LocalDate.of(2024, 3, 10)
    private val endDate = LocalDate.of(2024, 3, 14)

    // --- toDomain ---

    @Test
    fun `toDomain maps all fields from AbsenceEntity`() {
        // Arrange
        val entity = AbsenceEntity(
            id = id,
            userId = userId,
            startDate = startDate,
            endDate = endDate
        )

        // Act
        val domain = entity.toDomain()

        // Assert
        assertThat(domain.id).isEqualTo(id)
        assertThat(domain.userId).isEqualTo(userId)
        assertThat(domain.startDate).isEqualTo(startDate)
        assertThat(domain.endDate).isEqualTo(endDate)
    }

    @Test
    fun `toDomain preserves null endDate`() {
        // Arrange
        val entity = AbsenceEntity(
            id = id,
            userId = userId,
            startDate = startDate,
            endDate = null
        )

        // Act
        val domain = entity.toDomain()

        // Assert
        assertThat(domain.endDate).isNull()
    }

    @Test
    fun `toDomain returns Absence domain instance`() {
        // Arrange
        val entity = AbsenceEntity(id = id, userId = userId, startDate = startDate)

        // Act
        val domain = entity.toDomain()

        // Assert
        assertThat(domain).isInstanceOf(Absence::class.java)
    }

    @Test
    fun `toDomain produces independent objects on repeated calls`() {
        // Arrange
        val entity = AbsenceEntity(id = id, userId = userId, startDate = startDate, endDate = endDate)

        // Act
        val domain1 = entity.toDomain()
        val domain2 = entity.toDomain()

        // Assert
        assertThat(domain1).isEqualTo(domain2)
        assertThat(domain1).isNotSameAs(domain2)
    }

    // --- toEntity ---

    @Test
    fun `toEntity maps all fields from Absence domain`() {
        // Arrange
        val domain = Absence(
            id = id,
            userId = userId,
            startDate = startDate,
            endDate = endDate
        )

        // Act
        val entity = domain.toEntity()

        // Assert
        assertThat(entity.id).isEqualTo(id)
        assertThat(entity.userId).isEqualTo(userId)
        assertThat(entity.startDate).isEqualTo(startDate)
        assertThat(entity.endDate).isEqualTo(endDate)
    }

    @Test
    fun `toEntity preserves null endDate`() {
        // Arrange
        val domain = Absence(
            id = id,
            userId = userId,
            startDate = startDate,
            endDate = null
        )

        // Act
        val entity = domain.toEntity()

        // Assert
        assertThat(entity.endDate).isNull()
    }

    @Test
    fun `toEntity returns AbsenceEntity instance`() {
        // Arrange
        val domain = Absence(id = id, userId = userId, startDate = startDate)

        // Act
        val entity = domain.toEntity()

        // Assert
        assertThat(entity).isInstanceOf(AbsenceEntity::class.java)
    }

    // --- round-trip ---

    @Test
    fun `toDomain and toEntity are inverse operations`() {
        // Arrange
        val original = Absence(
            id = id,
            userId = userId,
            startDate = startDate,
            endDate = endDate
        )

        // Act
        val roundTripped = original.toEntity().toDomain()

        // Assert
        assertThat(roundTripped).isEqualTo(original)
    }

    @Test
    fun `toDomain and toEntity round-trip preserves null endDate`() {
        // Arrange
        val original = Absence(
            id = id,
            userId = userId,
            startDate = startDate,
            endDate = null
        )

        // Act
        val roundTripped = original.toEntity().toDomain()

        // Assert
        assertThat(roundTripped).isEqualTo(original)
    }
}
