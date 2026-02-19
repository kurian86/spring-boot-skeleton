package es.bdo.skeleton.shared.extension

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class UUIDExtensionTest {

    @Test
    fun `newUUID should generate non-null UUID`() {
        // Act
        val uuid = newUUID()

        // Assert
        assertThat(uuid).isNotNull()
    }

    @Test
    fun `newUUID should generate unique UUIDs`() {
        // Act
        val uuid1 = newUUID()
        val uuid2 = newUUID()
        val uuid3 = newUUID()

        // Assert
        assertThat(uuid1).isNotEqualTo(uuid2)
        assertThat(uuid2).isNotEqualTo(uuid3)
        assertThat(uuid1).isNotEqualTo(uuid3)
    }

    @Test
    fun `newUUID should generate valid UUID instance`() {
        // Act
        val uuid = newUUID()

        // Assert
        assertThat(uuid).isInstanceOf(UUID::class.java)
        assertThat(uuid.version()).isEqualTo(1)
    }

    @Test
    fun `newUUID should generate UUID with correct variant`() {
        // Act
        val uuid = newUUID()

        // Assert
        assertThat(uuid.variant()).isEqualTo(2)
    }

    @Test
    fun `newUUID should generate timestamp-based UUID`() {
        // Act
        val uuids = (1..10).map { newUUID() }

        // Assert
        val timestamps = uuids.map { it.timestamp() }
        assertThat(timestamps).isSorted()
    }

    @Test
    fun `generated UUIDs should have valid string representation`() {
        // Act
        val uuid = newUUID()
        val uuidString = uuid.toString()

        // Assert
        assertThat(uuidString).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
    }

    @Test
    fun `newUUID should generate UUIDs with version 1`() {
        // Act & Assert
        repeat(5) {
            val uuid = newUUID()
            assertThat(uuid.version()).isEqualTo(1)
        }
    }

    @Test
    fun `UUID timestamp should be positive and increasing`() {
        // Act
        val uuid1 = newUUID()
        val uuid2 = newUUID()

        // Assert
        assertThat(uuid1.timestamp()).isPositive()
        assertThat(uuid2.timestamp()).isPositive()
        assertThat(uuid2.timestamp()).isGreaterThanOrEqualTo(uuid1.timestamp())
    }
}
