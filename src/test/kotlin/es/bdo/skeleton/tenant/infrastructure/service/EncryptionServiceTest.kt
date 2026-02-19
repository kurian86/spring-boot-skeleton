package es.bdo.skeleton.tenant.infrastructure.service

import es.bdo.skeleton.tenant.infrastructure.config.SecurityProperties
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class EncryptionServiceTest {

    private lateinit var securityProperties: SecurityProperties
    private lateinit var encryptionService: EncryptionService

    companion object {
        private const val MASTER_KEY = "my-secret-master-key-for-testing-purposes-only-32chars!"
        private const val SALT = "test-salt-value-123"
    }

    @BeforeEach
    fun setUp() {
        securityProperties = mock(SecurityProperties::class.java)
        val encryption = mock(SecurityProperties.Encryption::class.java)
        `when`(securityProperties.encryption).thenReturn(encryption)
        `when`(encryption.masterKey).thenReturn(MASTER_KEY)
        `when`(encryption.salt).thenReturn(SALT)

        encryptionService = EncryptionService(securityProperties)
    }

    @Test
    fun `should encrypt and decrypt text successfully`() {
        // Arrange
        val plaintext = "Hello, World!"

        // Act
        val encrypted = encryptionService.encrypt(plaintext)
        val decrypted = encryptionService.decrypt(encrypted)

        // Assert
        assertThat(encrypted).isNotEqualTo(plaintext)
        assertThat(decrypted).isEqualTo(plaintext)
    }

    @Test
    fun `should produce different encrypted values for same plaintext`() {
        // Arrange
        val plaintext = "Test message"

        // Act
        val encrypted1 = encryptionService.encrypt(plaintext)
        val encrypted2 = encryptionService.encrypt(plaintext)

        // Assert
        assertThat(encrypted1).isNotEqualTo(encrypted2)
        assertThat(encryptionService.decrypt(encrypted1)).isEqualTo(plaintext)
        assertThat(encryptionService.decrypt(encrypted2)).isEqualTo(plaintext)
    }

    @Test
    fun `should handle empty string encryption`() {
        // Arrange
        val plaintext = ""

        // Act
        val encrypted = encryptionService.encrypt(plaintext)
        val decrypted = encryptionService.decrypt(encrypted)

        // Assert
        assertThat(decrypted).isEqualTo(plaintext)
    }

    @Test
    fun `should handle unicode characters`() {
        // Arrange
        val plaintext = "Hello ñáéíóú 中文 🎉"

        // Act
        val encrypted = encryptionService.encrypt(plaintext)
        val decrypted = encryptionService.decrypt(encrypted)

        // Assert
        assertThat(decrypted).isEqualTo(plaintext)
    }

    @Test
    fun `should handle long text`() {
        // Arrange
        val plaintext = "A".repeat(10000)

        // Act
        val encrypted = encryptionService.encrypt(plaintext)
        val decrypted = encryptionService.decrypt(encrypted)

        // Assert
        assertThat(decrypted).isEqualTo(plaintext)
    }

    @Test
    fun `should throw exception when decrypting invalid text`() {
        // Arrange
        val invalidText = "not-valid-base64!!!"

        // Act & Assert
        assertThrows<IllegalArgumentException> {
            encryptionService.decrypt(invalidText)
        }
    }

    @Test
    fun `should throw exception when decrypting tampered ciphertext`() {
        // Arrange
        val plaintext = "Secret message"
        val encrypted = encryptionService.encrypt(plaintext)
        val tampered = encrypted.dropLast(5) + "XXXXX"

        // Act & Assert
        assertThrows<Exception> {
            encryptionService.decrypt(tampered)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "short",
        "test123",
        "encrypted-text-with-valid-base64"
    ])
    fun `should return false for text that is not encrypted`(text: String) {
        // Act & Assert
        assertThat(encryptionService.isEncrypted(text)).isFalse()
    }

    @Test
    fun `should return true for encrypted text`() {
        // Arrange
        val plaintext = "Test"
        val encrypted = encryptionService.encrypt(plaintext)

        // Act & Assert
        assertThat(encryptionService.isEncrypted(encrypted)).isTrue()
    }

    @Test
    fun `should return false for empty string`() {
        assertThat(encryptionService.isEncrypted("")).isFalse()
    }

    @Test
    fun `should return false for invalid base64`() {
        assertThat(encryptionService.isEncrypted("!!!invalid!!!")).isFalse()
    }
}
