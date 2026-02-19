package es.bdo.skeleton.shared.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OperatorTest {

    @Test
    fun `should have all expected operators`() {
        // Arrange & Act
        val operators = Operator.entries.toTypedArray()

        // Assert
        assertThat(operators).containsExactly(
            Operator.EQUALITY,
            Operator.NEGATION,
            Operator.GREATER_EQUAL_THAN,
            Operator.GREATER_THAN,
            Operator.LESS_EQUAL_THAN,
            Operator.LESS_THAN,
            Operator.LIKE,
            Operator.STARTS_WITH,
            Operator.ENDS_WITH,
            Operator.CONTAINS,
            Operator.IS_NULL
        )
    }

    @Test
    fun `should have 11 operators`() {
        // Assert
        assertThat(Operator.entries.toTypedArray()).hasSize(11)
    }

    @Test
    fun `EQUALITY should be first operator`() {
        // Assert
        assertThat(Operator.EQUALITY).isEqualTo(Operator.entries[0])
    }

    @Test
    fun `should be able to retrieve operator by name`() {
        // Act
        val operator = Operator.valueOf("LIKE")

        // Assert
        assertThat(operator).isEqualTo(Operator.LIKE)
    }

    @Test
    fun `each operator should have unique name`() {
        // Arrange
        val operators = Operator.entries.toTypedArray()
        val names = operators.map { it.name }

        // Assert
        assertThat(names).hasSize(operators.size)
        assertThat(names.distinct()).hasSize(operators.size)
    }

    @Test
    fun `comparison operators should exist`() {
        // Assert
        assertThat(Operator.GREATER_THAN).isNotNull()
        assertThat(Operator.GREATER_EQUAL_THAN).isNotNull()
        assertThat(Operator.LESS_THAN).isNotNull()
        assertThat(Operator.LESS_EQUAL_THAN).isNotNull()
    }

    @Test
    fun `string matching operators should exist`() {
        // Assert
        assertThat(Operator.LIKE).isNotNull()
        assertThat(Operator.STARTS_WITH).isNotNull()
        assertThat(Operator.ENDS_WITH).isNotNull()
        assertThat(Operator.CONTAINS).isNotNull()
    }

    @Test
    fun `null checking operator should exist`() {
        // Assert
        assertThat(Operator.IS_NULL).isNotNull()
    }

    @Test
    fun `equality operators should exist`() {
        // Assert
        assertThat(Operator.EQUALITY).isNotNull()
        assertThat(Operator.NEGATION).isNotNull()
    }
}
