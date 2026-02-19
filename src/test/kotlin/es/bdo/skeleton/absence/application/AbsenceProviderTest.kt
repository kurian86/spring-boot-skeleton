package es.bdo.skeleton.absence.application

import es.bdo.skeleton.absence.domain.Absence
import es.bdo.skeleton.absence.domain.AbsenceRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.time.LocalDate
import java.util.UUID

class AbsenceProviderTest {

    private val repository: AbsenceRepository = mock()
    private val provider = AbsenceProvider(repository)

    private val userId = UUID.randomUUID()
    private val startDate = LocalDate.of(2024, 6, 1)
    private val endDate = LocalDate.of(2024, 6, 7)

    @Test
    fun `findAll returns empty list when repository has no absences`() {
        // Arrange
        `when`(repository.findAll(0, 10, null, emptyList())).thenReturn(0L to emptyList())

        // Act
        val (total, items) = provider.findAll()

        // Assert
        assertThat(total).isEqualTo(0L)
        assertThat(items).isEmpty()
    }

    @Test
    fun `findAll returns mapped DTOs for each absence`() {
        // Arrange
        val absences = listOf(
            Absence(id = UUID.randomUUID(), userId = userId, startDate = startDate, endDate = endDate),
            Absence(id = UUID.randomUUID(), userId = userId, startDate = startDate.plusDays(10), endDate = null)
        )
        `when`(repository.findAll(0, 10, null, emptyList())).thenReturn(2L to absences)

        // Act
        val (_, items) = provider.findAll()

        // Assert
        assertThat(items).hasSize(2)
    }

    @Test
    fun `findAll maps absence fields correctly to DTO`() {
        // Arrange
        val absenceId = UUID.randomUUID()
        val absence = Absence(id = absenceId, userId = userId, startDate = startDate, endDate = endDate)
        `when`(repository.findAll(0, 10, null, emptyList())).thenReturn(1L to listOf(absence))

        // Act
        val (_, items) = provider.findAll()

        // Assert
        val dto = items.single()
        assertThat(dto.id).isEqualTo(absenceId)
        assertThat(dto.userId).isEqualTo(userId)
        assertThat(dto.startDate).isEqualTo(startDate)
        assertThat(dto.endDate).isEqualTo(endDate)
    }

    @Test
    fun `findAll preserves null endDate in DTO`() {
        // Arrange
        val absence = Absence(id = UUID.randomUUID(), userId = userId, startDate = startDate, endDate = null)
        `when`(repository.findAll(0, 10, null, emptyList())).thenReturn(1L to listOf(absence))

        // Act
        val (_, items) = provider.findAll()

        // Assert
        assertThat(items.single().endDate).isNull()
    }

    @Test
    fun `findAll delegates to repository`() {
        // Arrange
        `when`(repository.findAll(0, 10, null, emptyList())).thenReturn(0L to emptyList())

        // Act
        provider.findAll()

        // Assert
        verify(repository).findAll(0, 10, null, emptyList())
    }
}
