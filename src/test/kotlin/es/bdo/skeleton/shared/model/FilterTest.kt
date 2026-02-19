package es.bdo.skeleton.shared.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FilterTest {

    @Test
    fun `should create Filter with default EQUALITY operator`() {
        // Arrange & Act
        val filter = Filter(property = "name", value = "John")

        // Assert
        assertThat(filter.property).isEqualTo("name")
        assertThat(filter.value).isEqualTo("John")
        assertThat(filter.operator).isEqualTo(Operator.EQUALITY)
    }

    @Test
    fun `should create Filter with custom operator`() {
        // Arrange & Act
        val filter = Filter(property = "age", value = "18", operator = Operator.GREATER_EQUAL_THAN)

        // Assert
        assertThat(filter.property).isEqualTo("age")
        assertThat(filter.value).isEqualTo("18")
        assertThat(filter.operator).isEqualTo(Operator.GREATER_EQUAL_THAN)
    }

    @Test
    fun `should create Filter with LIKE operator`() {
        // Arrange & Act
        val filter = Filter(property = "email", value = "@example.com", operator = Operator.LIKE)

        // Assert
        assertThat(filter.operator).isEqualTo(Operator.LIKE)
    }

    @Test
    fun `should create Filter with CONTAINS operator`() {
        // Arrange & Act
        val filter = Filter(property = "description", value = "test", operator = Operator.CONTAINS)

        // Assert
        assertThat(filter.operator).isEqualTo(Operator.CONTAINS)
    }

    @Test
    fun `should create Filter with STARTS_WITH operator`() {
        // Arrange & Act
        val filter = Filter(property = "name", value = "John", operator = Operator.STARTS_WITH)

        // Assert
        assertThat(filter.operator).isEqualTo(Operator.STARTS_WITH)
    }

    @Test
    fun `should create Filter with ENDS_WITH operator`() {
        // Arrange & Act
        val filter = Filter(property = "email", value = ".com", operator = Operator.ENDS_WITH)

        // Assert
        assertThat(filter.operator).isEqualTo(Operator.ENDS_WITH)
    }

    @Test
    fun `should create Filter with GREATER_THAN operator`() {
        // Arrange & Act
        val filter = Filter(property = "price", value = "100", operator = Operator.GREATER_THAN)

        // Assert
        assertThat(filter.operator).isEqualTo(Operator.GREATER_THAN)
    }

    @Test
    fun `should create Filter with LESS_THAN operator`() {
        // Arrange & Act
        val filter = Filter(property = "quantity", value = "10", operator = Operator.LESS_THAN)

        // Assert
        assertThat(filter.operator).isEqualTo(Operator.LESS_THAN)
    }

    @Test
    fun `should create Filter with LESS_EQUAL_THAN operator`() {
        // Arrange & Act
        val filter = Filter(property = "score", value = "100", operator = Operator.LESS_EQUAL_THAN)

        // Assert
        assertThat(filter.operator).isEqualTo(Operator.LESS_EQUAL_THAN)
    }

    @Test
    fun `should create Filter with NEGATION operator`() {
        // Arrange & Act
        val filter = Filter(property = "status", value = "inactive", operator = Operator.NEGATION)

        // Assert
        assertThat(filter.operator).isEqualTo(Operator.NEGATION)
    }

    @Test
    fun `should create Filter with IS_NULL operator`() {
        // Arrange & Act
        val filter = Filter(property = "deletedAt", value = "", operator = Operator.IS_NULL)

        // Assert
        assertThat(filter.operator).isEqualTo(Operator.IS_NULL)
    }

    @Test
    fun `Filter instances with same values should be equal`() {
        // Arrange
        val filter1 = Filter(property = "name", value = "John", operator = Operator.EQUALITY)
        val filter2 = Filter(property = "name", value = "John", operator = Operator.EQUALITY)

        // Assert
        assertThat(filter1).isEqualTo(filter2)
        assertThat(filter1.hashCode()).isEqualTo(filter2.hashCode())
    }

    @Test
    fun `Filter instances with different values should not be equal`() {
        // Arrange
        val filter1 = Filter(property = "name", value = "John", operator = Operator.EQUALITY)
        val filter2 = Filter(property = "name", value = "Jane", operator = Operator.EQUALITY)

        // Assert
        assertThat(filter1).isNotEqualTo(filter2)
    }
}
