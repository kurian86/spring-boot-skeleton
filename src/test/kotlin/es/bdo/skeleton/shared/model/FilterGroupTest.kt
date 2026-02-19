package es.bdo.skeleton.shared.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FilterGroupTest {

    @Test
    fun `should create FilterGroup with empty filters by default`() {
        // Arrange & Act
        val filterGroup = FilterGroup()

        // Assert
        assertThat(filterGroup.filters).isEmpty()
    }

    @Test
    fun `should create FilterGroup with provided filters`() {
        // Arrange
        val filters = listOf(
            Filter(property = "name", value = "John", operator = Operator.EQUALITY),
            Filter(property = "age", value = "25", operator = Operator.GREATER_EQUAL_THAN)
        )

        // Act
        val filterGroup = FilterGroup(filters = filters)

        // Assert
        assertThat(filterGroup.filters).hasSize(2)
        assertThat(filterGroup.filters[0].property).isEqualTo("name")
        assertThat(filterGroup.filters[1].property).isEqualTo("age")
    }

    @Test
    fun `should create FilterGroup with single filter`() {
        // Arrange
        val filter = Filter(property = "email", value = "test@example.com", operator = Operator.EQUALITY)

        // Act
        val filterGroup = FilterGroup(filters = listOf(filter))

        // Assert
        assertThat(filterGroup.filters).hasSize(1)
        assertThat(filterGroup.filters[0].property).isEqualTo("email")
    }

    @Test
    fun `FilterGroup instances with same filters should be equal`() {
        // Arrange
        val filters = listOf(Filter(property = "name", value = "John", operator = Operator.EQUALITY))
        val group1 = FilterGroup(filters = filters)
        val group2 = FilterGroup(filters = filters)

        // Assert
        assertThat(group1).isEqualTo(group2)
        assertThat(group1.hashCode()).isEqualTo(group2.hashCode())
    }

    @Test
    fun `FilterGroup instances with different filters should not be equal`() {
        // Arrange
        val group1 = FilterGroup(filters = listOf(Filter(property = "name", value = "John", operator = Operator.EQUALITY)))
        val group2 = FilterGroup(filters = listOf(Filter(property = "name", value = "Jane", operator = Operator.EQUALITY)))

        // Assert
        assertThat(group1).isNotEqualTo(group2)
    }

    @Test
    fun `should support multiple FilterGroups`() {
        // Arrange
        val group1 = FilterGroup(
            filters = listOf(Filter(property = "status", value = "active", operator = Operator.EQUALITY))
        )
        val group2 = FilterGroup(
            filters = listOf(
                Filter(property = "age", value = "18", operator = Operator.GREATER_EQUAL_THAN),
                Filter(property = "age", value = "65", operator = Operator.LESS_EQUAL_THAN)
            )
        )

        // Act
        val groups = listOf(group1, group2)

        // Assert
        assertThat(groups).hasSize(2)
        assertThat(groups[0].filters).hasSize(1)
        assertThat(groups[1].filters).hasSize(2)
    }
}
