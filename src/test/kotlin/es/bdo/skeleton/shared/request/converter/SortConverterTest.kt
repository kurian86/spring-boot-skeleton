package es.bdo.skeleton.shared.request.converter

import tools.jackson.databind.ObjectMapper
import es.bdo.skeleton.shared.model.Sort
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Sort as SpringSort

class SortConverterTest {

    private val objectMapper = ObjectMapper()
    private val converter = SortConverter(objectMapper)

    @Test
    fun `should convert valid JSON to Sort with ASC direction`() {
        // Arrange
        val json = """{"property":"name","direction":"ASC"}"""

        // Act
        val result = converter.convert(json)

        // Assert
        assertThat(result).isNotNull()
        assertThat(result!!.property).isEqualTo("name")
        assertThat(result.direction).isEqualTo(SpringSort.Direction.ASC)
    }

    @Test
    fun `should convert valid JSON to Sort with DESC direction`() {
        // Arrange
        val json = """{"property":"createdAt","direction":"DESC"}"""

        // Act
        val result = converter.convert(json)

        // Assert
        assertThat(result).isNotNull()
        assertThat(result!!.property).isEqualTo("createdAt")
        assertThat(result.direction).isEqualTo(SpringSort.Direction.DESC)
    }

    @Test
    fun `should return null for invalid JSON`() {
        // Arrange
        val invalidJson = "not valid json"

        // Act
        val result = converter.convert(invalidJson)

        // Assert
        assertThat(result).isNull()
    }

    @Test
    fun `should return null for malformed JSON`() {
        // Arrange
        val malformedJson = "{\"property\""

        // Act
        val result = converter.convert(malformedJson)

        // Assert
        assertThat(result).isNull()
    }

    @Test
    fun `should return null for empty string`() {
        // Arrange
        val emptyJson = ""

        // Act
        val result = converter.convert(emptyJson)

        // Assert
        assertThat(result).isNull()
    }

    @Test
    fun `should return null for JSON without property field`() {
        // Arrange
        val json = """{"direction":"ASC"}"""

        // Act
        val result = converter.convert(json)

        // Assert - property is required, so deserialization fails
        assertThat(result).isNull()
    }

    @Test
    fun `should return null for JSON without direction field`() {
        // Arrange
        val json = """{"property":"name"}"""

        // Act
        val result = converter.convert(json)

        // Assert - direction is required, so deserialization fails
        assertThat(result).isNull()
    }

    @Test
    fun `should handle various property names`() {
        // Arrange
        val properties = listOf("id", "name", "email", "createdAt", "updatedAt", "status")

        // Act & Assert
        properties.forEach { property ->
            val json = """{"property":"$property","direction":"ASC"}"""
            val result = converter.convert(json)
            assertThat(result!!.property).isEqualTo(property)
        }
    }

    @Test
    fun `should handle all Spring Sort directions`() {
        // Arrange
        val directions = listOf("ASC", "DESC")

        // Act & Assert
        directions.forEach { direction ->
            val json = """{"property":"name","direction":"$direction"}"""
            val result = converter.convert(json)
            assertThat(result!!.direction).isEqualTo(SpringSort.Direction.valueOf(direction))
        }
    }

    @Test
    fun `should return null for JSON with invalid direction`() {
        // Arrange
        val json = """{"property":"name","direction":"INVALID"}"""

        // Act
        val result = converter.convert(json)

        // Assert
        assertThat(result).isNull()
    }

    @Test
    fun `should return null for JSON with null values`() {
        // Arrange
        val json = """{"property":null,"direction":null}"""

        // Act
        val result = converter.convert(json)

        // Assert - null values for required fields cause deserialization to fail
        assertThat(result).isNull()
    }

    @Test
    fun `should return null for empty JSON object`() {
        // Arrange
        val json = "{}"

        // Act
        val result = converter.convert(json)

        // Assert - missing required fields cause deserialization to fail
        assertThat(result).isNull()
    }
}
