package es.bdo.skeleton.user.domain

import es.bdo.skeleton.shared.model.FilterGroup
import es.bdo.skeleton.shared.model.Sort
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class UserRepositoryTest {

    private val repository = mock<UserRepository>()

    @Test
    fun `findAll with pagination and filters should be callable`() {
        // Arrange
        val filters = emptyList<FilterGroup>()
        val sort = null
        
        whenever(repository.findAll(0, 10, sort, filters)).thenReturn(emptyList())
        
        // Act & Assert - should compile and not throw
        val result = repository.findAll(0, 10, sort, filters)
        
        assert(result.isEmpty())
    }
    
    @Test
    fun `count with filters should be callable`() {
        // Arrange
        val filters = emptyList<FilterGroup>()
        
        whenever(repository.count(filters)).thenReturn(0)
        
        // Act & Assert
        val result = repository.count(filters)
        
        assert(result == 0L)
    }
}
