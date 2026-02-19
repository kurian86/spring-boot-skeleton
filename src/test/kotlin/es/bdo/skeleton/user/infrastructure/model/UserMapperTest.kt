package es.bdo.skeleton.user.infrastructure.model

import es.bdo.skeleton.user.domain.User
import es.bdo.skeleton.user.domain.UserStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import java.util.UUID

class UserMapperTest {

    private val fixedTime = ZonedDateTime.now()
    private val id = UUID.randomUUID()

    private fun entity(externalId: String? = "ext-1") = UserEntity(
        id = id,
        name = "Bob",
        email = "bob@example.com",
        status = UserStatus.ACTIVE,
        externalId = externalId,
        createdAt = fixedTime,
        updatedAt = fixedTime,
    )

    private fun domain(externalId: String? = "ext-1") = User(
        id = id,
        name = "Bob",
        email = "bob@example.com",
        status = UserStatus.ACTIVE,
        externalId = externalId,
        createdAt = fixedTime,
        updatedAt = fixedTime,
    )

    // --- toDomain ---

    @Test
    fun `toDomain maps id correctly`() {
        assertThat(entity().toDomain().id).isEqualTo(id)
    }

    @Test
    fun `toDomain maps name correctly`() {
        assertThat(entity().toDomain().name).isEqualTo("Bob")
    }

    @Test
    fun `toDomain maps email correctly`() {
        assertThat(entity().toDomain().email).isEqualTo("bob@example.com")
    }

    @Test
    fun `toDomain maps status correctly`() {
        assertThat(entity().toDomain().status).isEqualTo(UserStatus.ACTIVE)
    }

    @Test
    fun `toDomain maps externalId correctly`() {
        assertThat(entity(externalId = "ext-42").toDomain().externalId).isEqualTo("ext-42")
    }

    @Test
    fun `toDomain maps null externalId correctly`() {
        assertThat(entity(externalId = null).toDomain().externalId).isNull()
    }

    @Test
    fun `toDomain maps timestamps correctly`() {
        val result = entity().toDomain()
        assertThat(result.createdAt).isEqualTo(fixedTime)
        assertThat(result.updatedAt).isEqualTo(fixedTime)
    }

    // --- toEntity ---

    @Test
    fun `toEntity maps all fields correctly`() {
        val result = domain().toEntity()
        assertThat(result.id).isEqualTo(id)
        assertThat(result.name).isEqualTo("Bob")
        assertThat(result.email).isEqualTo("bob@example.com")
        assertThat(result.status).isEqualTo(UserStatus.ACTIVE)
        assertThat(result.externalId).isEqualTo("ext-1")
        assertThat(result.createdAt).isEqualTo(fixedTime)
        assertThat(result.updatedAt).isEqualTo(fixedTime)
    }

    @Test
    fun `toEntity maps null externalId correctly`() {
        assertThat(domain(externalId = null).toEntity().externalId).isNull()
    }

    // --- round-trip ---

    @Test
    fun `toDomain then toEntity round-trips correctly`() {
        val original = entity()
        val roundTripped = original.toDomain().toEntity()
        assertThat(roundTripped).isEqualTo(original)
    }
}
