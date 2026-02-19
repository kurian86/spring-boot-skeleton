package es.bdo.skeleton.shared.request

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Sort

class OffsetLimitPageableTest {

    @Test
    fun `should create OffsetLimitPageable with default values`() {
        // Arrange & Act
        val pageable = OffsetLimitPageable(offset = 0)

        // Assert
        assertThat(pageable.offset).isEqualTo(0)
        assertThat(pageable.pageSize).isEqualTo(OffsetLimitPageable.DEFAULT_LIMIT)
    }

    @Test
    fun `should create OffsetLimitPageable with custom offset and limit`() {
        // Arrange & Act
        val pageable = OffsetLimitPageable(offset = 20, limit = 25)

        // Assert
        assertThat(pageable.offset).isEqualTo(20)
        assertThat(pageable.pageSize).isEqualTo(25)
    }

    @Test
    fun `should create OffsetLimitPageable with custom sort`() {
        // Arrange
        val sort = Sort.by(Sort.Direction.DESC, "createdAt")

        // Act
        val pageable = OffsetLimitPageable(offset = 0, limit = 10, sort = sort)

        // Assert
        assertThat(pageable.sort).isEqualTo(sort)
        assertThat(pageable.sort.isSorted).isTrue()
    }

    @Test
    fun `should throw exception when offset is negative`() {
        // Arrange & Act & Assert
        assertThatThrownBy { OffsetLimitPageable(offset = -1) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Offset must not be less than zero")
    }

    @Test
    fun `should throw exception when limit is less than one`() {
        // Arrange & Act & Assert
        assertThatThrownBy { OffsetLimitPageable(offset = 0, limit = 0) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Page size must not be less than one")
    }

    @Test
    fun `should throw exception when limit is negative`() {
        // Arrange & Act & Assert
        assertThatThrownBy { OffsetLimitPageable(offset = 0, limit = -5) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Page size must not be less than one")
    }

    @Test
    fun `getPageNumber should calculate correct page number`() {
        // Arrange & Act & Assert
        assertThat(OffsetLimitPageable(offset = 0, limit = 10).pageNumber).isEqualTo(0)
        assertThat(OffsetLimitPageable(offset = 10, limit = 10).pageNumber).isEqualTo(1)
        assertThat(OffsetLimitPageable(offset = 20, limit = 10).pageNumber).isEqualTo(2)
        assertThat(OffsetLimitPageable(offset = 25, limit = 10).pageNumber).isEqualTo(2)
    }

    @Test
    fun `next should return pageable with incremented offset`() {
        // Arrange
        val pageable = OffsetLimitPageable(offset = 20, limit = 10)

        // Act
        val nextPageable = pageable.next()

        // Assert
        assertThat(nextPageable.offset).isEqualTo(30)
        assertThat(nextPageable.pageSize).isEqualTo(10)
    }

    @Test
    fun `previousOrFirst should return previous when hasPrevious is true`() {
        // Arrange
        val pageable = OffsetLimitPageable(offset = 20, limit = 10)

        // Act
        val previousPageable = pageable.previousOrFirst()

        // Assert
        assertThat(previousPageable.offset).isEqualTo(10)
    }

    @Test
    fun `previousOrFirst should return first when hasPrevious is false`() {
        // Arrange
        val pageable = OffsetLimitPageable(offset = 0, limit = 10)

        // Act
        val firstPageable = pageable.previousOrFirst()

        // Assert
        assertThat(firstPageable.offset).isEqualTo(0)
    }

    @Test
    fun `first should return pageable with offset zero`() {
        // Arrange
        val pageable = OffsetLimitPageable(offset = 50, limit = 10)

        // Act
        val firstPageable = pageable.first()

        // Assert
        assertThat(firstPageable.offset).isEqualTo(0)
        assertThat(firstPageable.pageSize).isEqualTo(10)
    }

    @Test
    fun `withPage should return pageable for given page number`() {
        // Arrange
        val pageable = OffsetLimitPageable(offset = 0, limit = 10)

        // Act & Assert
        assertThat(pageable.withPage(0).offset).isEqualTo(0)
        assertThat(pageable.withPage(1).offset).isEqualTo(10)
        assertThat(pageable.withPage(2).offset).isEqualTo(20)
        assertThat(pageable.withPage(5).offset).isEqualTo(50)
    }

    @Test
    fun `hasPrevious should return false when offset is zero`() {
        // Arrange
        val pageable = OffsetLimitPageable(offset = 0, limit = 10)

        // Assert
        assertThat(pageable.hasPrevious()).isFalse()
    }

    @Test
    fun `hasPrevious should return false when offset less than limit`() {
        // Arrange
        val pageable = OffsetLimitPageable(offset = 5, limit = 10)

        // Assert
        assertThat(pageable.hasPrevious()).isFalse()
    }

    @Test
    fun `hasPrevious should return true when offset equals limit`() {
        // Arrange
        val pageable = OffsetLimitPageable(offset = 10, limit = 10)

        // Assert
        assertThat(pageable.hasPrevious()).isTrue()
    }

    @Test
    fun `hasPrevious should return true when offset greater than limit`() {
        // Arrange
        val pageable = OffsetLimitPageable(offset = 25, limit = 10)

        // Assert
        assertThat(pageable.hasPrevious()).isTrue()
    }

    @Test
    fun `should preserve sort when navigating pages`() {
        // Arrange
        val sort = Sort.by(Sort.Direction.ASC, "name")
        val pageable = OffsetLimitPageable(offset = 20, limit = 10, sort = sort)

        // Act
        val nextPageable = pageable.next()
        val previousPageable = pageable.previousOrFirst()
        val firstPageable = pageable.first()

        // Assert
        assertThat(nextPageable.sort).isEqualTo(sort)
        assertThat(previousPageable.sort).isEqualTo(sort)
        assertThat(firstPageable.sort).isEqualTo(sort)
    }

    @Test
    fun `should preserve limit when navigating pages`() {
        // Arrange
        val pageable = OffsetLimitPageable(offset = 30, limit = 25)

        // Act
        val nextPageable = pageable.next()
        val previousPageable = pageable.previousOrFirst()
        val firstPageable = pageable.first()

        // Assert
        assertThat(nextPageable.pageSize).isEqualTo(25)
        assertThat(previousPageable.pageSize).isEqualTo(25)
        assertThat(firstPageable.pageSize).isEqualTo(25)
    }

    @Test
    fun `getSort should return unsorted when no sort provided`() {
        // Arrange
        val pageable = OffsetLimitPageable(offset = 0)

        // Assert
        assertThat(pageable.sort.isUnsorted).isTrue()
    }
}
