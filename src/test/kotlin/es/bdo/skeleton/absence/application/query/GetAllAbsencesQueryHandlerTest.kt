package es.bdo.skeleton.absence.application.query

import es.bdo.skeleton.absence.domain.Absence
import es.bdo.skeleton.absence.domain.AbsenceRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.time.LocalDate
import java.util.UUID

class GetAllAbsencesQueryHandlerTest {

    private val repository: AbsenceRepository = mock()
    private val handler = GetAllAbsencesQueryHandler(repository)

    private val userId = UUID.randomUUID()
    private val startDate = LocalDate.of(2024, 5, 1)
    private val endDate = LocalDate.of(2024, 5, 5)

    // --- handle ---

    @Test
    fun `handle returns success with empty PaginationResult when repository is empty`() {
        // Arrange
        `when`(repository.findAll(0, 10, null, emptyList())).thenReturn(0L to emptyList())

        // Act
        val result = handler.handle(GetAllAbsencesQuery())

        // Assert
        assertThat(result.isSuccess).isTrue()
        val pagination = result.getOrThrow()
        assertThat(pagination.totalCount).isEqualTo(0L)
        assertThat(pagination.items).isEmpty()
    }

    @Test
    fun `handle returns correct totalCount from repository`() {
        // Arrange
        `when`(repository.findAll(0, 10, null, emptyList())).thenReturn(3L to emptyList())

        // Act
        val result = handler.handle(GetAllAbsencesQuery())

        // Assert
        assertThat(result.getOrThrow().totalCount).isEqualTo(3L)
    }

    @Test
    fun `handle maps absences to DTOs`() {
        // Arrange
        val absence = Absence(id = UUID.randomUUID(), userId = userId, startDate = startDate, endDate = endDate)
        `when`(repository.findAll(0, 10, null, emptyList())).thenReturn(1L to listOf(absence))

        // Act
        val result = handler.handle(GetAllAbsencesQuery())

        // Assert
        val items = result.getOrThrow().items
        assertThat(items).hasSize(1)
        assertThat(items[0].userId).isEqualTo(userId)
        assertThat(items[0].startDate).isEqualTo(startDate)
        assertThat(items[0].endDate).isEqualTo(endDate)
    }

    @Test
    fun `handle maps absence with null endDate to DTO`() {
        // Arrange
        val absence = Absence(id = UUID.randomUUID(), userId = userId, startDate = startDate, endDate = null)
        `when`(repository.findAll(0, 10, null, emptyList())).thenReturn(1L to listOf(absence))

        // Act
        val result = handler.handle(GetAllAbsencesQuery())

        // Assert
        assertThat(result.getOrThrow().items[0].endDate).isNull()
    }

    @Test
    fun `handle returns all items from repository`() {
        // Arrange
        val absences = (1..3).map {
            Absence(id = UUID.randomUUID(), userId = userId, startDate = startDate.plusDays(it.toLong()))
        }
        `when`(repository.findAll(0, 10, null, emptyList())).thenReturn(3L to absences)

        // Act
        val result = handler.handle(GetAllAbsencesQuery())

        // Assert
        assertThat(result.getOrThrow().items).hasSize(3)
    }

    @Test
    fun `handle returns failure when repository throws exception`() {
        // Arrange
        `when`(repository.findAll(0, 10, null, emptyList())).thenThrow(RuntimeException("DB error"))

        // Act
        val result = handler.handle(GetAllAbsencesQuery())

        // Assert
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(RuntimeException::class.java)
        assertThat(result.exceptionOrNull()?.message).isEqualTo("DB error")
    }

    @Test
    fun `handle calls findAll on repository with query parameters`() {
        // Arrange
        `when`(repository.findAll(0, 10, null, emptyList())).thenReturn(0L to emptyList())

        // Act
        handler.handle(GetAllAbsencesQuery())

        // Assert
        verify(repository).findAll(0, 10, null, emptyList())
    }
}
