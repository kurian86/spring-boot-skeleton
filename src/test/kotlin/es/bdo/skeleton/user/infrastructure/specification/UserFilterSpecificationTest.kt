package es.bdo.skeleton.user.infrastructure.specification

import es.bdo.skeleton.shared.model.Filter
import es.bdo.skeleton.shared.model.FilterGroup
import es.bdo.skeleton.shared.model.Operator
import es.bdo.skeleton.user.infrastructure.model.UserEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.data.jpa.domain.Specification

class UserFilterSpecificationTest {

    private val specification = UserFilterSpecification()

    @Test
    fun `toSpecification with EQUALITY filter on status should create valid spec`() {
        // Arrange
        val filters = listOf(
            FilterGroup(
                filters = listOf(
                    Filter(property = "status", value = "ACTIVE", operator = Operator.EQUALITY)
                )
            )
        )

        // Act
        val spec: Specification<UserEntity>? = specification.toSpecification(filters)

        // Assert
        assertThat(spec).isNotNull
    }

    @Test
    fun `toSpecification with LIKE filter on name should create valid spec`() {
        // Arrange
        val filters = listOf(
            FilterGroup(
                filters = listOf(
                    Filter(property = "name", value = "John", operator = Operator.LIKE)
                )
            )
        )

        // Act
        val spec: Specification<UserEntity>? = specification.toSpecification(filters)

        // Assert
        assertThat(spec).isNotNull
    }

    @Test
    fun `toSpecification with invalid property should throw IllegalArgumentException`() {
        // Arrange
        val filters = listOf(
            FilterGroup(
                filters = listOf(
                    Filter(property = "invalidField", value = "test", operator = Operator.EQUALITY)
                )
            )
        )

        // Act & Assert
        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            specification.toSpecification(filters)
        }
    }
}
