package es.bdo.skeleton.shared.request.converter

import tools.jackson.databind.ObjectMapper
import es.bdo.skeleton.shared.model.Filter
import es.bdo.skeleton.shared.model.FilterGroup
import es.bdo.skeleton.shared.model.Operator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ArrayFilterGroupConverterTest {

    private val objectMapper = ObjectMapper()
    private val converter = ArrayFilterGroupConverter(objectMapper)

    @Test
    fun `should convert valid JSON array to FilterGroup array`() {
        // Arrange
        val json = """[{"filters":[{"property":"name","value":"John","operator":"EQUALITY"}]}]"""

        // Act
        val result = converter.convert(json)

        // Assert
        assertThat(result).hasSize(1)
        assertThat(result[0].filters).hasSize(1)
        assertThat(result[0].filters[0].property).isEqualTo("name")
        assertThat(result[0].filters[0].value).isEqualTo("John")
        assertThat(result[0].filters[0].operator).isEqualTo(Operator.EQUALITY)
    }

    @Test
    fun `should convert JSON with multiple filter groups`() {
        // Arrange
        val json = """[
            {"filters":[{"property":"status","value":"active","operator":"EQUALITY"}]},
            {"filters":[{"property":"age","value":"18","operator":"GREATER_EQUAL_THAN"}]}
        ]"""

        // Act
        val result = converter.convert(json)

        // Assert
        assertThat(result).hasSize(2)
        assertThat(result[0].filters[0].property).isEqualTo("status")
        assertThat(result[1].filters[0].property).isEqualTo("age")
    }

    @Test
    fun `should convert JSON with multiple filters in single group`() {
        // Arrange
        val json = """[{
            "filters":[
                {"property":"name","value":"John","operator":"EQUALITY"},
                {"property":"email","value":"john@example.com","operator":"EQUALITY"}
            ]
        }]"""

        // Act
        val result = converter.convert(json)

        // Assert
        assertThat(result).hasSize(1)
        assertThat(result[0].filters).hasSize(2)
        assertThat(result[0].filters[0].property).isEqualTo("name")
        assertThat(result[0].filters[1].property).isEqualTo("email")
    }

    @Test
    fun `should return empty array for invalid JSON`() {
        // Arrange
        val invalidJson = "not valid json"

        // Act
        val result = converter.convert(invalidJson)

        // Assert
        assertThat(result).isEmpty()
    }

    @Test
    fun `should return empty array for malformed JSON`() {
        // Arrange
        val malformedJson = "{\"invalid"

        // Act
        val result = converter.convert(malformedJson)

        // Assert
        assertThat(result).isEmpty()
    }

    @Test
    fun `should return empty array for empty string`() {
        // Arrange
        val emptyJson = ""

        // Act
        val result = converter.convert(emptyJson)

        // Assert
        assertThat(result).isEmpty()
    }

    @Test
    fun `should return empty array for JSON without filters field`() {
        // Arrange
        val json = """[{"invalidField":"value"}]"""

        // Act
        val result = converter.convert(json)

        // Assert - Jackson fails to deserialize without required fields, returning empty array
        assertThat(result).isEmpty()
    }

    @Test
    fun `should convert JSON with all operator types`() {
        // Arrange
        val json = """[{
            "filters":[
                {"property":"field1","value":"value1","operator":"EQUALITY"},
                {"property":"field2","value":"value2","operator":"NEGATION"},
                {"property":"field3","value":"value3","operator":"GREATER_THAN"},
                {"property":"field4","value":"value4","operator":"GREATER_EQUAL_THAN"},
                {"property":"field5","value":"value5","operator":"LESS_THAN"},
                {"property":"field6","value":"value6","operator":"LESS_EQUAL_THAN"},
                {"property":"field7","value":"value7","operator":"LIKE"},
                {"property":"field8","value":"value8","operator":"STARTS_WITH"},
                {"property":"field9","value":"value9","operator":"ENDS_WITH"},
                {"property":"field10","value":"value10","operator":"CONTAINS"},
                {"property":"field11","value":"value11","operator":"IS_NULL"}
            ]
        }]"""

        // Act
        val result = converter.convert(json)

        // Assert
        assertThat(result[0].filters).hasSize(11)
        assertThat(result[0].filters.map { it.operator }).containsExactly(
            Operator.EQUALITY,
            Operator.NEGATION,
            Operator.GREATER_THAN,
            Operator.GREATER_EQUAL_THAN,
            Operator.LESS_THAN,
            Operator.LESS_EQUAL_THAN,
            Operator.LIKE,
            Operator.STARTS_WITH,
            Operator.ENDS_WITH,
            Operator.CONTAINS,
            Operator.IS_NULL
        )
    }

    @Test
    fun `should handle empty JSON array`() {
        // Arrange
        val json = "[]"

        // Act
        val result = converter.convert(json)

        // Assert
        assertThat(result).isEmpty()
    }

    @Test
    fun `should handle JSON array with empty filter group`() {
        // Arrange
        val json = """[{"filters":[]}]"""

        // Act
        val result = converter.convert(json)

        // Assert
        assertThat(result).hasSize(1)
        assertThat(result[0].filters).isEmpty()
    }
}
