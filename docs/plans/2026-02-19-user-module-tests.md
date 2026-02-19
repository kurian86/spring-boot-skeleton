# User Module Test Suite — Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Write a complete TDD test suite for the `user` module covering domain, application, and infrastructure layers.

**Architecture:** Each test file targets one class/concern. Unit tests use plain JUnit 5 + Mockito-Kotlin. The JPA slice test uses `@DataJpaTest` + H2 wired to `tenantEntityManagerFactory`/`tenantTransactionManager` — the same pattern as `AbsenceRepositorySliceTest`.

**Tech Stack:** Kotlin, JUnit 5, Mockito-Kotlin, AssertJ, Spring Boot `@DataJpaTest`, H2

---

## Reference files

Before writing any test, read these files for exact patterns to mirror:

- `src/test/kotlin/es/bdo/skeleton/absence/infrastructure/AbsenceRepositorySliceTest.kt` — JPA slice pattern
- `src/test/kotlin/es/bdo/skeleton/absence/application/query/GetAllAbsencesQueryHandlerTest.kt` — handler unit test pattern
- `src/test/kotlin/es/bdo/skeleton/absence/application/AbsenceProviderTest.kt` — service/provider unit test pattern

## Source files to understand

- `src/main/kotlin/es/bdo/skeleton/user/domain/User.kt`
- `src/main/kotlin/es/bdo/skeleton/user/domain/UserRepository.kt`
- `src/main/kotlin/es/bdo/skeleton/user/domain/UserStatus.kt`
- `src/main/kotlin/es/bdo/skeleton/user/application/model/UserDTO.kt`
- `src/main/kotlin/es/bdo/skeleton/user/application/model/UserStatusDTO.kt`
- `src/main/kotlin/es/bdo/skeleton/user/application/query/GetAllUserQueryHandler.kt`
- `src/main/kotlin/es/bdo/skeleton/user/application/UserRegistrationService.kt`
- `src/main/kotlin/es/bdo/skeleton/user/application/exception/UserDisabledException.kt`
- `src/main/kotlin/es/bdo/skeleton/user/infrastructure/model/UserEntity.kt`
- `src/main/kotlin/es/bdo/skeleton/user/infrastructure/UserJpaRepository.kt`
- `src/main/kotlin/es/bdo/skeleton/user/infrastructure/UserRepository.kt`

---

## Task 1: `UserDisabledExceptionTest`

**Files:**
- Create: `src/test/kotlin/es/bdo/skeleton/user/application/exception/UserDisabledExceptionTest.kt`

**Step 1: Write the failing test**

```kotlin
package es.bdo.skeleton.user.application.exception

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.security.core.AuthenticationException

class UserDisabledExceptionTest {

    @Test
    fun `default message is set correctly`() {
        // Arrange + Act
        val ex = UserDisabledException()

        // Assert
        assertThat(ex.message).isEqualTo("User account is disabled")
    }

    @Test
    fun `custom message overrides default`() {
        // Arrange + Act
        val ex = UserDisabledException("Account suspended")

        // Assert
        assertThat(ex.message).isEqualTo("Account suspended")
    }

    @Test
    fun `is an AuthenticationException`() {
        // Arrange + Act
        val ex = UserDisabledException()

        // Assert
        assertThat(ex).isInstanceOf(AuthenticationException::class.java)
    }
}
```

**Step 2: Run test to verify it compiles and passes**

```bash
./gradlew test --tests "es.bdo.skeleton.user.application.exception.UserDisabledExceptionTest" --rerun-tasks
```

Expected: 3 tests PASS (production class already exists — no RED phase needed; tests prove existing behaviour).

**Step 3: Commit**

```bash
git add src/test/kotlin/es/bdo/skeleton/user/application/exception/UserDisabledExceptionTest.kt
git commit -m "test(user): add UserDisabledException unit tests"
```

---

## Task 2: `UserStatusDTOTest`

**Files:**
- Create: `src/test/kotlin/es/bdo/skeleton/user/application/model/UserStatusDTOTest.kt`

**Step 1: Write the failing test**

```kotlin
package es.bdo.skeleton.user.application.model

import es.bdo.skeleton.user.domain.UserStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UserStatusDTOTest {

    @Test
    fun `ACTIVE maps to ACTIVE`() {
        // Arrange + Act
        val result = UserStatus.ACTIVE.toDTO()

        // Assert
        assertThat(result).isEqualTo(UserStatusDTO.ACTIVE)
    }

    @Test
    fun `DISABLED maps to DISABLED`() {
        // Arrange + Act
        val result = UserStatus.DISABLED.toDTO()

        // Assert
        assertThat(result).isEqualTo(UserStatusDTO.DISABLED)
    }

    @Test
    fun `all UserStatus values have a DTO mapping`() {
        // Arrange + Act + Assert
        UserStatus.entries.forEach { status ->
            assertThat(status.toDTO()).isNotNull()
        }
    }
}
```

**Step 2: Run test to verify it passes**

```bash
./gradlew test --tests "es.bdo.skeleton.user.application.model.UserStatusDTOTest" --rerun-tasks
```

Expected: 3 tests PASS.

**Step 3: Commit**

```bash
git add src/test/kotlin/es/bdo/skeleton/user/application/model/UserStatusDTOTest.kt
git commit -m "test(user): add UserStatusDTO mapper tests"
```

---

## Task 3: `UserDTOTest`

**Files:**
- Create: `src/test/kotlin/es/bdo/skeleton/user/application/model/UserDTOTest.kt`

**Step 1: Write the failing test**

```kotlin
package es.bdo.skeleton.user.application.model

import es.bdo.skeleton.user.domain.User
import es.bdo.skeleton.user.domain.UserStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import java.util.UUID

class UserDTOTest {

    private val fixedTime = ZonedDateTime.now()
    private val userId = UUID.randomUUID()

    private fun user(
        status: UserStatus = UserStatus.ACTIVE,
        externalId: String? = null,
    ) = User(
        id = userId,
        name = "Alice",
        email = "alice@example.com",
        status = status,
        externalId = externalId,
        createdAt = fixedTime,
        updatedAt = fixedTime,
    )

    @Test
    fun `toDTO maps id correctly`() {
        assertThat(user().toDTO().id).isEqualTo(userId)
    }

    @Test
    fun `toDTO maps name correctly`() {
        assertThat(user().toDTO().name).isEqualTo("Alice")
    }

    @Test
    fun `toDTO maps email correctly`() {
        assertThat(user().toDTO().email).isEqualTo("alice@example.com")
    }

    @Test
    fun `toDTO maps ACTIVE status correctly`() {
        assertThat(user(status = UserStatus.ACTIVE).toDTO().status).isEqualTo(UserStatusDTO.ACTIVE)
    }

    @Test
    fun `toDTO maps DISABLED status correctly`() {
        assertThat(user(status = UserStatus.DISABLED).toDTO().status).isEqualTo(UserStatusDTO.DISABLED)
    }

    @Test
    fun `toDTO sets roles to ROLE_USER`() {
        assertThat(user().toDTO().roles).containsExactly("ROLE_USER")
    }

    @Test
    fun `toDTO maps createdAt correctly`() {
        assertThat(user().toDTO().createdAt).isEqualTo(fixedTime)
    }

    @Test
    fun `toDTO maps updatedAt correctly`() {
        assertThat(user().toDTO().updatedAt).isEqualTo(fixedTime)
    }
}
```

**Step 2: Run test to verify it passes**

```bash
./gradlew test --tests "es.bdo.skeleton.user.application.model.UserDTOTest" --rerun-tasks
```

Expected: 8 tests PASS.

**Step 3: Commit**

```bash
git add src/test/kotlin/es/bdo/skeleton/user/application/model/UserDTOTest.kt
git commit -m "test(user): add UserDTO mapper tests"
```

---

## Task 4: `UserMapperTest`

**Files:**
- Create: `src/test/kotlin/es/bdo/skeleton/user/infrastructure/model/UserMapperTest.kt`

**Step 1: Write the failing test**

```kotlin
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
```

**Step 2: Run test to verify it passes**

```bash
./gradlew test --tests "es.bdo.skeleton.user.infrastructure.model.UserMapperTest" --rerun-tasks
```

Expected: 10 tests PASS.

**Step 3: Commit**

```bash
git add src/test/kotlin/es/bdo/skeleton/user/infrastructure/model/UserMapperTest.kt
git commit -m "test(user): add UserEntity mapper tests"
```

---

## Task 5: `GetAllUserQueryHandlerTest`

**Files:**
- Create: `src/test/kotlin/es/bdo/skeleton/user/application/query/GetAllUserQueryHandlerTest.kt`

**Step 1: Write the failing test**

```kotlin
package es.bdo.skeleton.user.application.query

import es.bdo.skeleton.user.application.model.UserStatusDTO
import es.bdo.skeleton.user.domain.User
import es.bdo.skeleton.user.domain.UserRepository
import es.bdo.skeleton.user.domain.UserStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.ZonedDateTime
import java.util.UUID

class GetAllUserQueryHandlerTest {

    private val repository: UserRepository = mock()
    private val handler = GetAllUserQueryHandler(repository)

    private fun user(name: String = "Alice") = User(
        id = UUID.randomUUID(),
        name = name,
        email = "$name@example.com",
        status = UserStatus.ACTIVE,
        createdAt = ZonedDateTime.now(),
        updatedAt = ZonedDateTime.now(),
    )

    @Test
    fun `handle returns empty PaginationResult when repository is empty`() {
        // Arrange
        whenever(repository.count()).thenReturn(0L)
        whenever(repository.findAll()).thenReturn(emptyList())

        // Act
        val result = handler.handle(GetAllUserQuery()).getOrThrow()

        // Assert
        assertThat(result.totalCount).isEqualTo(0L)
        assertThat(result.items).isEmpty()
    }

    @Test
    fun `handle returns correct totalCount`() {
        // Arrange
        whenever(repository.count()).thenReturn(3L)
        whenever(repository.findAll()).thenReturn(listOf(user(), user(), user()))

        // Act
        val result = handler.handle(GetAllUserQuery()).getOrThrow()

        // Assert
        assertThat(result.totalCount).isEqualTo(3L)
    }

    @Test
    fun `handle maps users to DTOs`() {
        // Arrange
        val u = user("Bob")
        whenever(repository.count()).thenReturn(1L)
        whenever(repository.findAll()).thenReturn(listOf(u))

        // Act
        val result = handler.handle(GetAllUserQuery()).getOrThrow()

        // Assert
        assertThat(result.items).hasSize(1)
        assertThat(result.items.first().name).isEqualTo("Bob")
        assertThat(result.items.first().email).isEqualTo("Bob@example.com")
        assertThat(result.items.first().status).isEqualTo(UserStatusDTO.ACTIVE)
    }

    @Test
    fun `handle sets roles to ROLE_USER for every user`() {
        // Arrange
        whenever(repository.count()).thenReturn(2L)
        whenever(repository.findAll()).thenReturn(listOf(user(), user()))

        // Act
        val result = handler.handle(GetAllUserQuery()).getOrThrow()

        // Assert
        result.items.forEach { dto ->
            assertThat(dto.roles).containsExactly("ROLE_USER")
        }
    }

    @Test
    fun `handle returns Result failure when repository throws`() {
        // Arrange
        whenever(repository.count()).thenThrow(RuntimeException("DB error"))

        // Act
        val result = handler.handle(GetAllUserQuery())

        // Assert
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).hasMessage("DB error")
    }

    @Test
    fun `handle items count matches list size`() {
        // Arrange
        whenever(repository.count()).thenReturn(2L)
        whenever(repository.findAll()).thenReturn(listOf(user(), user()))

        // Act
        val result = handler.handle(GetAllUserQuery()).getOrThrow()

        // Assert
        assertThat(result.items).hasSize(2)
    }
}
```

**Step 2: Run test to verify it passes**

```bash
./gradlew test --tests "es.bdo.skeleton.user.application.query.GetAllUserQueryHandlerTest" --rerun-tasks
```

Expected: 6 tests PASS.

**Step 3: Commit**

```bash
git add src/test/kotlin/es/bdo/skeleton/user/application/query/GetAllUserQueryHandlerTest.kt
git commit -m "test(user): add GetAllUserQueryHandler unit tests"
```

---

## Task 6: `UserRegistrationServiceTest`

**Files:**
- Create: `src/test/kotlin/es/bdo/skeleton/user/application/UserRegistrationServiceTest.kt`

**Step 1: Write the failing test**

```kotlin
package es.bdo.skeleton.user.application

import es.bdo.skeleton.user.domain.User
import es.bdo.skeleton.user.domain.UserRepository
import es.bdo.skeleton.user.domain.UserStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.ZonedDateTime
import java.util.UUID

class UserRegistrationServiceTest {

    private val repository: UserRepository = mock()
    private val service = UserRegistrationService(repository)

    private fun existingUser(email: String = "alice@example.com") = User(
        id = UUID.randomUUID(),
        name = "Alice",
        email = email,
        status = UserStatus.ACTIVE,
        createdAt = ZonedDateTime.now(),
        updatedAt = ZonedDateTime.now(),
    )

    @Test
    fun `ensureUserExists returns existing user when email is found`() {
        // Arrange
        val user = existingUser()
        whenever(repository.findByEmail(user.email)).thenReturn(user)

        // Act
        val result = service.ensureUserExists(user.email, "Alice", null)

        // Assert
        assertThat(result).isEqualTo(user)
        verify(repository, never()).save(any())
    }

    @Test
    fun `ensureUserExists does not call save when user already exists`() {
        // Arrange
        val user = existingUser()
        whenever(repository.findByEmail(user.email)).thenReturn(user)

        // Act
        service.ensureUserExists(user.email, "Alice", null)

        // Assert
        verify(repository, never()).save(any())
    }

    @Test
    fun `ensureUserExists creates and saves new user when email not found`() {
        // Arrange
        whenever(repository.findByEmail("new@example.com")).thenReturn(null)
        whenever(repository.save(any())).thenAnswer { it.arguments[0] as User }

        // Act
        val result = service.ensureUserExists("new@example.com", "New User", null)

        // Assert
        verify(repository).save(any())
        assertThat(result.email).isEqualTo("new@example.com")
        assertThat(result.name).isEqualTo("New User")
    }

    @Test
    fun `ensureUserExists creates new user with ACTIVE status`() {
        // Arrange
        whenever(repository.findByEmail(any())).thenReturn(null)
        whenever(repository.save(any())).thenAnswer { it.arguments[0] as User }

        // Act
        val result = service.ensureUserExists("x@example.com", "X", null)

        // Assert
        assertThat(result.status).isEqualTo(UserStatus.ACTIVE)
    }

    @Test
    fun `ensureUserExists propagates externalId to new user`() {
        // Arrange
        whenever(repository.findByEmail(any())).thenReturn(null)
        whenever(repository.save(any())).thenAnswer { it.arguments[0] as User }

        // Act
        val result = service.ensureUserExists("x@example.com", "X", "ext-123")

        // Assert
        assertThat(result.externalId).isEqualTo("ext-123")
    }

    @Test
    fun `ensureUserExists propagates null externalId to new user`() {
        // Arrange
        whenever(repository.findByEmail(any())).thenReturn(null)
        whenever(repository.save(any())).thenAnswer { it.arguments[0] as User }

        // Act
        val result = service.ensureUserExists("x@example.com", "X", null)

        // Assert
        assertThat(result.externalId).isNull()
    }
}
```

**Step 2: Run test to verify it passes**

```bash
./gradlew test --tests "es.bdo.skeleton.user.application.UserRegistrationServiceTest" --rerun-tasks
```

Expected: 6 tests PASS.

**Step 3: Commit**

```bash
git add src/test/kotlin/es/bdo/skeleton/user/application/UserRegistrationServiceTest.kt
git commit -m "test(user): add UserRegistrationService unit tests"
```

---

## Task 7: `UserRepositorySliceTest`

**Files:**
- Create: `src/test/kotlin/es/bdo/skeleton/user/infrastructure/UserRepositorySliceTest.kt`

**Notes before writing:**
- Mirror `AbsenceRepositorySliceTest` exactly for the `@DataJpaTest` + H2 wiring
- Use `tenantEntityManagerFactory` / `tenantTransactionManager` bean names
- `UserJpaRepository.findByEmail()` returns `Optional<UserEntity>` — test `.isPresent` / `.isEmpty`
- `findAll()` returns `Iterable<UserEntity>` — call `.toList()`
- `UserStatus` is `@Enumerated(EnumType.STRING)` — verify it round-trips

**Step 1: Write the failing test**

```kotlin
package es.bdo.skeleton.user.infrastructure

import es.bdo.skeleton.user.domain.UserStatus
import es.bdo.skeleton.user.infrastructure.model.UserEntity
import jakarta.persistence.EntityManagerFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jpa.EntityManagerFactoryBuilder
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import java.time.ZonedDateTime
import java.util.UUID
import javax.sql.DataSource

@DataJpaTest(
    properties = [
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
    ]
)
@AutoConfigureTestDatabase
@ContextConfiguration(classes = [UserRepositorySliceTest.TestConfig::class])
class UserRepositorySliceTest {

    @Configuration
    @EnableTransactionManagement
    @EnableJpaRepositories(
        basePackageClasses = [UserJpaRepository::class],
        entityManagerFactoryRef = "tenantEntityManagerFactory",
        transactionManagerRef = "tenantTransactionManager"
    )
    class TestConfig {

        @Bean
        fun tenantEntityManagerFactory(
            builder: EntityManagerFactoryBuilder,
            dataSource: DataSource
        ): LocalContainerEntityManagerFactoryBean =
            builder
                .dataSource(dataSource)
                .packages("es.bdo.skeleton.user.infrastructure.model")
                .persistenceUnit("tenant")
                .build()

        @Bean
        fun tenantTransactionManager(
            tenantEntityManagerFactory: EntityManagerFactory
        ): PlatformTransactionManager =
            JpaTransactionManager(tenantEntityManagerFactory)
    }

    @Autowired
    private lateinit var jpaRepository: UserJpaRepository

    @BeforeEach
    fun setUp() {
        jpaRepository.deleteAll()
    }

    private fun entity(
        email: String = "alice@example.com",
        status: UserStatus = UserStatus.ACTIVE,
        externalId: String? = null,
    ) = UserEntity(
        id = UUID.randomUUID(),
        name = "Alice",
        email = email,
        status = status,
        externalId = externalId,
        createdAt = ZonedDateTime.now(),
        updatedAt = ZonedDateTime.now(),
    )

    // --- count ---

    @Test
    fun `count returns zero when repository is empty`() {
        // Arrange

        // Act
        val count = jpaRepository.count()

        // Assert
        assertThat(count).isEqualTo(0L)
    }

    @Test
    fun `count returns correct number of saved users`() {
        // Arrange
        jpaRepository.saveAll(
            listOf(
                entity(email = "a@example.com"),
                entity(email = "b@example.com"),
                entity(email = "c@example.com"),
            )
        )

        // Act
        val count = jpaRepository.count()

        // Assert
        assertThat(count).isEqualTo(3L)
    }

    @Test
    fun `count reflects deletions`() {
        // Arrange
        val saved = jpaRepository.save(entity())
        jpaRepository.delete(saved)

        // Act
        val count = jpaRepository.count()

        // Assert
        assertThat(count).isEqualTo(0L)
    }

    // --- findAll ---

    @Test
    fun `findAll returns empty list when repository is empty`() {
        // Arrange

        // Act
        val result = jpaRepository.findAll().toList()

        // Assert
        assertThat(result).isEmpty()
    }

    @Test
    fun `findAll returns all saved users`() {
        // Arrange
        jpaRepository.saveAll(
            listOf(
                entity(email = "a@example.com"),
                entity(email = "b@example.com"),
            )
        )

        // Act
        val result = jpaRepository.findAll().toList()

        // Assert
        assertThat(result).hasSize(2)
    }

    @Test
    fun `findAll persists all fields correctly`() {
        // Arrange
        val original = entity(email = "full@example.com", status = UserStatus.DISABLED, externalId = "ext-99")
        jpaRepository.save(original)

        // Act
        val result = jpaRepository.findAll().toList().single()

        // Assert
        assertThat(result.id).isEqualTo(original.id)
        assertThat(result.name).isEqualTo("Alice")
        assertThat(result.email).isEqualTo("full@example.com")
        assertThat(result.status).isEqualTo(UserStatus.DISABLED)
        assertThat(result.externalId).isEqualTo("ext-99")
    }

    @Test
    fun `findAll persists null externalId correctly`() {
        // Arrange
        jpaRepository.save(entity(externalId = null))

        // Act
        val result = jpaRepository.findAll().toList().single()

        // Assert
        assertThat(result.externalId).isNull()
    }

    @Test
    fun `findAll preserves UserStatus enum as string`() {
        // Arrange
        jpaRepository.save(entity(status = UserStatus.DISABLED))

        // Act
        val result = jpaRepository.findAll().toList().single()

        // Assert
        assertThat(result.status).isEqualTo(UserStatus.DISABLED)
    }

    // --- findByEmail ---

    @Test
    fun `findByEmail returns user when email matches`() {
        // Arrange
        jpaRepository.save(entity(email = "find@example.com"))

        // Act
        val result = jpaRepository.findByEmail("find@example.com")

        // Assert
        assertThat(result).isPresent
        assertThat(result.get().email).isEqualTo("find@example.com")
    }

    @Test
    fun `findByEmail returns empty Optional when email not found`() {
        // Arrange

        // Act
        val result = jpaRepository.findByEmail("missing@example.com")

        // Assert
        assertThat(result).isEmpty
    }

    @Test
    fun `findByEmail is case-sensitive`() {
        // Arrange
        jpaRepository.save(entity(email = "case@example.com"))

        // Act
        val result = jpaRepository.findByEmail("CASE@example.com")

        // Assert
        assertThat(result).isEmpty
    }
}
```

**Step 2: Run test to verify it passes**

```bash
./gradlew test --tests "es.bdo.skeleton.user.infrastructure.UserRepositorySliceTest" --rerun-tasks
```

Expected: 11 tests PASS.

**Step 3: Commit**

```bash
git add src/test/kotlin/es/bdo/skeleton/user/infrastructure/UserRepositorySliceTest.kt
git commit -m "test(user): add UserRepository JPA slice tests"
```

---

## Task 8: Verify full test suite

Run all tests to confirm no regressions.

```bash
./gradlew test --rerun-tasks
```

Expected: BUILD SUCCESSFUL, 0 failures.

**Commit if not already done:**

```bash
git commit --allow-empty -m "test(user): all user module tests green"
```
