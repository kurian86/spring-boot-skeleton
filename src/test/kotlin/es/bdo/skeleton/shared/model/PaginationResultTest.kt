package es.bdo.skeleton.shared.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PaginationResultTest {

    @Test
    fun `from creates PaginationResult with correct totalCount`() {
        // Arrange
        val totalCount = 100L
        val items = listOf("item1", "item2")

        // Act
        val result = PaginationResult.from(totalCount, items, 0, 10)

        // Assert
        assertThat(result.totalCount).isEqualTo(totalCount)
    }

    @Test
    fun `from creates PaginationResult with correct items`() {
        // Arrange
        val items = listOf("item1", "item2", "item3")

        // Act
        val result = PaginationResult.from(10, items, 0, 10)

        // Assert
        assertThat(result.items).isEqualTo(items)
    }

    @Test
    fun `from sets hasNextPage to true when more items exist`() {
        // Arrange
        val totalCount = 100L
        val offset = 0L
        val limit = 10

        // Act
        val result = PaginationResult.from(totalCount, emptyList<String>(), offset, limit)

        // Assert
        assertThat(result.pageInfo.hasNextPage).isTrue()
    }

    @Test
    fun `from sets hasNextPage to false when at end`() {
        // Arrange
        val totalCount = 10L
        val offset = 0L
        val limit = 10

        // Act
        val result = PaginationResult.from(totalCount, emptyList<String>(), offset, limit)

        // Assert
        assertThat(result.pageInfo.hasNextPage).isFalse()
    }

    @Test
    fun `from sets hasNextPage to false when offset plus limit equals total`() {
        // Arrange
        val totalCount = 20L
        val offset = 10L
        val limit = 10

        // Act
        val result = PaginationResult.from(totalCount, emptyList<String>(), offset, limit)

        // Assert
        assertThat(result.pageInfo.hasNextPage).isFalse()
    }

    @Test
    fun `from sets hasNextPage to true when offset plus limit less than total`() {
        // Arrange
        val totalCount = 25L
        val offset = 10L
        val limit = 10

        // Act
        val result = PaginationResult.from(totalCount, emptyList<String>(), offset, limit)

        // Assert
        assertThat(result.pageInfo.hasNextPage).isTrue()
    }

    @Test
    fun `from sets hasPreviousPage to false when offset is zero`() {
        // Arrange
        val offset = 0L

        // Act
        val result = PaginationResult.from(100, emptyList<String>(), offset, 10)

        // Assert
        assertThat(result.pageInfo.hasPreviousPage).isFalse()
    }

    @Test
    fun `from sets hasPreviousPage to true when offset greater than zero`() {
        // Arrange
        val offset = 10L

        // Act
        val result = PaginationResult.from(100, emptyList<String>(), offset, 10)

        // Assert
        assertThat(result.pageInfo.hasPreviousPage).isTrue()
    }

    @Test
    fun `default constructor creates empty PaginationResult`() {
        // Act
        val result = PaginationResult<String>()

        // Assert
        assertThat(result.totalCount).isEqualTo(0)
        assertThat(result.items).isEmpty()
        assertThat(result.pageInfo.hasNextPage).isFalse()
        assertThat(result.pageInfo.hasPreviousPage).isFalse()
    }

    @Test
    fun `from works with generic types`() {
        // Arrange
        data class TestItem(val id: Int, val name: String)
        val items = listOf(TestItem(1, "Item 1"), TestItem(2, "Item 2"))

        // Act
        val result = PaginationResult.from(10, items, 0, 10)

        // Assert
        assertThat(result.items).hasSize(2)
        assertThat(result.items[0].name).isEqualTo("Item 1")
    }
}
