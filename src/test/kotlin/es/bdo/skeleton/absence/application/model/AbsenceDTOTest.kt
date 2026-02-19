package es.bdo.skeleton.absence.application.model

import es.bdo.skeleton.absence.domain.Absence
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class AbsenceDTOTest {

    private val userId = UUID.randomUUID()
    private val absenceId = UUID.randomUUID()
    private val startDate = LocalDate.of(2024, 1, 15)
    private val endDate = LocalDate.of(2024, 1, 20)

    // --- toDTO ---

    @Test
    fun `toDTO maps all fields from domain Absence`() {
        // Arrange
        val absence = Absence(
            id = absenceId,
            userId = userId,
            startDate = startDate,
            endDate = endDate
        )

        // Act
        val dto = absence.toDTO()

        // Assert
        assertThat(dto.id).isEqualTo(absenceId)
        assertThat(dto.userId).isEqualTo(userId)
        assertThat(dto.startDate).isEqualTo(startDate)
        assertThat(dto.endDate).isEqualTo(endDate)
    }

    @Test
    fun `toDTO preserves null endDate`() {
        // Arrange
        val absence = Absence(
            id = absenceId,
            userId = userId,
            startDate = startDate,
            endDate = null
        )

        // Act
        val dto = absence.toDTO()

        // Assert
        assertThat(dto.endDate).isNull()
    }

    @Test
    fun `toDTO returns AbsenceDTO instance`() {
        // Arrange
        val absence = Absence(
            id = absenceId,
            userId = userId,
            startDate = startDate,
            endDate = endDate
        )

        // Act
        val dto = absence.toDTO()

        // Assert
        assertThat(dto).isInstanceOf(AbsenceDTO::class.java)
    }

    @Test
    fun `toDTO produces independent object from domain`() {
        // Arrange
        val absence = Absence(
            id = absenceId,
            userId = userId,
            startDate = startDate,
            endDate = endDate
        )

        // Act
        val dto1 = absence.toDTO()
        val dto2 = absence.toDTO()

        // Assert
        assertThat(dto1).isEqualTo(dto2)
        assertThat(dto1).isNotSameAs(dto2)
    }

    // --- AbsenceDTO data class ---

    @Test
    fun `AbsenceDTO stores all constructor fields`() {
        // Arrange & Act
        val dto = AbsenceDTO(
            id = absenceId,
            userId = userId,
            startDate = startDate,
            endDate = endDate
        )

        // Assert
        assertThat(dto.id).isEqualTo(absenceId)
        assertThat(dto.userId).isEqualTo(userId)
        assertThat(dto.startDate).isEqualTo(startDate)
        assertThat(dto.endDate).isEqualTo(endDate)
    }

    @Test
    fun `AbsenceDTO allows null endDate`() {
        // Arrange & Act
        val dto = AbsenceDTO(
            id = absenceId,
            userId = userId,
            startDate = startDate,
            endDate = null
        )

        // Assert
        assertThat(dto.endDate).isNull()
    }
}
