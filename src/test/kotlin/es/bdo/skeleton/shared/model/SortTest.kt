package es.bdo.skeleton.shared.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Sort as SpringSort

class SortTest {

    @Test
    fun `should create Sort with ASC direction`() {
        // Arrange & Act
        val sort = Sort(property = "name", direction = SpringSort.Direction.ASC)

        // Assert
        assertThat(sort.property).isEqualTo("name")
        assertThat(sort.direction).isEqualTo(SpringSort.Direction.ASC)
    }

    @Test
    fun `should create Sort with DESC direction`() {
        // Arrange & Act
        val sort = Sort(property = "createdAt", direction = SpringSort.Direction.DESC)

        // Assert
        assertThat(sort.property).isEqualTo("createdAt")
        assertThat(sort.direction).isEqualTo(SpringSort.Direction.DESC)
    }

    @Test
    fun `should create Sort with different properties`() {
        // Arrange
        val properties = listOf("id", "name", "email", "createdAt", "updatedAt")

        // Act & Assert
        properties.forEach { property ->
            val sort = Sort(property = property, direction = SpringSort.Direction.ASC)
            assertThat(sort.property).isEqualTo(property)
        }
    }

    @Test
    fun `Sort instances with same values should be equal`() {
        // Arrange
        val sort1 = Sort(property = "name", direction = SpringSort.Direction.ASC)
        val sort2 = Sort(property = "name", direction = SpringSort.Direction.ASC)

        // Assert
        assertThat(sort1).isEqualTo(sort2)
        assertThat(sort1.hashCode()).isEqualTo(sort2.hashCode())
    }

    @Test
    fun `Sort instances with different properties should not be equal`() {
        // Arrange
        val sort1 = Sort(property = "name", direction = SpringSort.Direction.ASC)
        val sort2 = Sort(property = "email", direction = SpringSort.Direction.ASC)

        // Assert
        assertThat(sort1).isNotEqualTo(sort2)
    }

    @Test
    fun `Sort instances with different directions should not be equal`() {
        // Arrange
        val sort1 = Sort(property = "name", direction = SpringSort.Direction.ASC)
        val sort2 = Sort(property = "name", direction = SpringSort.Direction.DESC)

        // Assert
        assertThat(sort1).isNotEqualTo(sort2)
    }

    @Test
    fun `should work with Spring Sort Direction values`() {
        // Act & Assert
        SpringSort.Direction.values().forEach { direction ->
            val sort = Sort(property = "field", direction = direction)
            assertThat(sort.direction).isEqualTo(direction)
        }
    }
}
