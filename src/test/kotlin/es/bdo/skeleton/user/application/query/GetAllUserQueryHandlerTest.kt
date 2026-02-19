package es.bdo.skeleton.user.application.query

import es.bdo.skeleton.shared.model.Filter
import es.bdo.skeleton.shared.model.FilterGroup
import es.bdo.skeleton.shared.model.Operator
import es.bdo.skeleton.shared.request.OffsetLimitPageable
import es.bdo.skeleton.user.application.model.UserStatusDTO
import es.bdo.skeleton.user.domain.User
import es.bdo.skeleton.user.domain.UserRepository
import es.bdo.skeleton.user.domain.UserStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.ZonedDateTime
import java.util.UUID

class GetAllUserQueryHandlerTest {

    private val repository: UserRepository = mock()
    private val handler = GetAllUserQueryHandler(repository)

    private fun user(name: String = "Alice") = User(
        id = UUID.randomUUID(),
        name = name,
        email = "$name@example.com",
        status = UserStatus.ACTIVE,
        createdAt = ZonedDateTime.now(),
        updatedAt = ZonedDateTime.now(),
    )

    @Test
    fun `handle returns empty PaginationResult when repository is empty`() {
        // Arrange
        whenever(repository.count(emptyList())).thenReturn(0L)
        whenever(repository.findAll(0, 10, null, emptyList())).thenReturn(emptyList())

        // Act
        val result = handler.handle(GetAllUserQuery()).getOrThrow()

        // Assert
        assertThat(result.totalCount).isEqualTo(0L)
        assertThat(result.items).isEmpty()
    }

    @Test
    fun `handle returns correct totalCount`() {
        // Arrange
        whenever(repository.count(emptyList())).thenReturn(3L)
        whenever(repository.findAll(0, 10, null, emptyList())).thenReturn(listOf(user(), user(), user()))

        // Act
        val result = handler.handle(GetAllUserQuery()).getOrThrow()

        // Assert
        assertThat(result.totalCount).isEqualTo(3L)
    }

    @Test
    fun `handle maps users to DTOs`() {
        // Arrange
        val u = user("Bob")
        whenever(repository.count(emptyList())).thenReturn(1L)
        whenever(repository.findAll(0, 10, null, emptyList())).thenReturn(listOf(u))

        // Act
        val result = handler.handle(GetAllUserQuery()).getOrThrow()

        // Assert
        assertThat(result.items).hasSize(1)
        assertThat(result.items.first().name).isEqualTo("Bob")
        assertThat(result.items.first().email).isEqualTo("Bob@example.com")
        assertThat(result.items.first().status).isEqualTo(UserStatusDTO.ACTIVE)
    }

    @Test
    fun `handle sets roles to ROLE_USER for every user`() {
        // Arrange
        whenever(repository.count(emptyList())).thenReturn(2L)
        whenever(repository.findAll(0, 10, null, emptyList())).thenReturn(listOf(user(), user()))

        // Act
        val result = handler.handle(GetAllUserQuery()).getOrThrow()

        // Assert
        result.items.forEach { dto ->
            assertThat(dto.roles).containsExactly("ROLE_USER")
        }
    }

    @Test
    fun `handle returns Result failure when repository throws`() {
        // Arrange
        whenever(repository.count(emptyList())).thenThrow(RuntimeException("DB error"))

        // Act
        val result = handler.handle(GetAllUserQuery())

        // Assert
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).hasMessage("DB error")
    }

    @Test
    fun `handle items count matches list size`() {
        // Arrange
        whenever(repository.count(emptyList())).thenReturn(2L)
        whenever(repository.findAll(0, 10, null, emptyList())).thenReturn(listOf(user(), user()))

        // Act
        val result = handler.handle(GetAllUserQuery()).getOrThrow()

        // Assert
        assertThat(result.items).hasSize(2)
    }

    @Test
    fun `handle with pagination should call repository with correct parameters`() {
        // Arrange
        val offset = 0L
        val limit = 10
        val query = GetAllUserQuery(
            pageable = OffsetLimitPageable(offset = offset, limit = limit),
            sort = null,
            filters = emptyList()
        )
        
        whenever(repository.count(emptyList())).thenReturn(100)
        whenever(repository.findAll(offset, limit, null, emptyList())).thenReturn(emptyList())

        // Act
        handler.handle(query)

        // Assert
        verify(repository).findAll(offset, limit, null, emptyList())
        verify(repository).count(emptyList())
    }

    @Test
    fun `handle with filters should call repository with filters`() {
        // Arrange
        val filters = listOf(
            FilterGroup(
                filters = listOf(
                    Filter(property = "status", value = "ACTIVE", operator = Operator.EQUALITY)
                )
            )
        )
        val query = GetAllUserQuery(
            pageable = OffsetLimitPageable(0),
            sort = null,
            filters = filters
        )
        
        val u = user("John")
        whenever(repository.count(filters)).thenReturn(1)
        whenever(repository.findAll(0, 10, null, filters)).thenReturn(listOf(u))

        // Act
        val result = handler.handle(query).getOrThrow()

        // Assert
        assertThat(result.items).hasSize(1)
        assertThat(result.totalCount).isEqualTo(1)
        verify(repository).findAll(0, 10, null, filters)
        verify(repository).count(filters)
    }
}
