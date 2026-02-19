# Agent Guidelines

This document provides coding agents with the commands, conventions, and architecture rules needed to work effectively in this repository.

## Project Overview

A multi-tenant Spring Boot 4 skeleton using Clean Architecture (Hexagonal/Ports & Adapters) with Spring Modulith. Built with Kotlin 2.3, JVM 25 (GraalVM), Spring Data JPA, PostgreSQL, Flyway, OAuth2/JWT via ZITADEL, and Caffeine caching. See `ARCHITECTURE.md` for the full design document.

## Build & Test Commands

```bash
# Run all tests
./gradlew test

# Run all checks (tests + ArchUnit architecture rules)
./gradlew check

# Run a single test class
./gradlew test --tests "es.bdo.skeleton.tenant.infrastructure.TenantRepositorySliceTest"

# Run a single test method (use backtick-quoted method name)
./gradlew test --tests "es.bdo.skeleton.tenant.infrastructure.TenantRepositorySliceTest.findAllByIsActive returns only active tenants when querying active"

# Build JVM Docker image
./docker.sh build-jvm

# Build GraalVM native Docker image (requires MSVC on Windows)
./docker.sh build-native   # Linux/Mac
build-native.bat           # Windows

# Start dev infrastructure (PostgreSQL + ZITADEL)
./docker.sh compose-up

# Stop dev infrastructure
./docker.sh compose-down
```

No Makefile. No Checkstyle/Spotless. Formatting is enforced by convention (see below).

## Architecture Rules (Enforced by ArchUnit)

These rules are verified in `ArchitectureTests.kt` and will cause CI failures if broken:

1. **Dependency direction:** `Infrastructure → Application → Domain` (inward only).
2. **Domain is pure:** No Spring annotations, no JPA, no infrastructure concerns.
3. **Cross-module access:** Modules (`user`, `tenant`, `absence`) may only access other modules through their `application` layer (DTOs + Providers). Never touch another module's `domain` or `infrastructure`.
4. **Shared kernel:** `shared/**` is accessible to all layers and modules without restriction.

## Module Structure

Each business module follows this layout:

```
<module>/
├── domain/          # Pure Kotlin data classes + repository interfaces (NO Spring)
├── application/     # Use cases (CQRS handlers), DTOs, Providers, exceptions
│   ├── model/       # DTOs (e.g. UserDTO)
│   ├── query/       # QueryHandler implementations
│   ├── command/     # CommandHandler implementations
│   └── exception/   # Domain/application exceptions
└── infrastructure/  # Controllers, JPA entities, JPA repos, config, security
    ├── controller/  # @RestController classes
    ├── model/       # JPA @Entity classes + mapper extension functions
    ├── config/      # @Configuration classes
    └── service/     # Infrastructure services (e.g. EncryptionService)
```

## CQRS Pattern

Use `QueryHandler` and `CommandHandler` from `shared/cqrs/` for all use cases:

```kotlin
// Query handler (returns Result<R>)
@Component
class GetAllUserQueryHandler(
    private val userRepository: UserRepository,
) : QueryHandler<GetAllUserQuery, PaginationResult<UserDTO>> {
    override fun handle(query: GetAllUserQuery): Result<PaginationResult<UserDTO>> =
        runCatching { PaginationResult.from(userRepository.findAll()) }
}

// Controller calls .getOrThrow() or .fold(...)
fun index(): PaginationResult<UserDTO> =
    getAllUserQueryHandler.handle(GetAllUserQuery()).getOrThrow()
```

## Naming Conventions

| Artifact | Convention | Example |
|---|---|---|
| Domain model | `data class` PascalCase | `User`, `Absence` |
| Domain repository interface | `interface <Entity>Repository` | `UserRepository` |
| JPA entity | `<Entity>Entity` | `UserEntity` |
| JPA repository | `<Entity>JpaRepository` | `UserJpaRepository` |
| Infrastructure repository impl | same name as domain, different package | `UserRepository` in `infrastructure/` |
| DTO | `<Entity>DTO` | `UserDTO` |
| Query/Command object | `<Action><Entity>Query/Command` | `GetAllUserQuery` |
| Query/Command handler | `<Action><Entity>QueryHandler` | `GetAllUserQueryHandler` |
| Provider (cross-module) | `<Entity>Provider` | `ConfigProvider` |
| Controller | `<Entity>Controller` | `UserController` |
| Package | `es.bdo.skeleton.<module>.<layer>` | `es.bdo.skeleton.user.application` |

## Code Style

**Language & formatting**
- Kotlin idiomatic style. No explicit `return` type when inferrable from a single expression.
- Single-expression functions with `=` for simple handlers and mappers.
- `data class` for domain models and DTOs. No mutable state in domain.
- `ZonedDateTime` for all timestamps. `UUID` for all primary keys (time-based v1 via `newUUID()` from `shared/extension/UUIDExtension.kt`).
- Compiler flag `-Xjsr305=strict` is active — treat all `@Nullable`/`@NonNull` annotations as strict.

**Imports**
- No wildcard imports.
- Standard library imports before third-party; third-party before project imports.
- No unused imports.

**Dependency injection**
- Constructor injection only. Never use `@Autowired` on fields.
- `private val` for all injected dependencies.
- Use `@Component`, `@Service`, `@Repository`, `@RestController` (never plain `@Bean` for business classes).

**Null safety**
- Prefer non-nullable types. Use `?` only when null is a meaningful value.
- Never use `!!` unless a null here is a programming bug that should crash loudly.
- Use `?.let`, `?: return`, `?: throw` for null handling.

**Error handling**
- Handlers return `Result<T>` using `runCatching { ... }`.
- Controllers call `.getOrThrow()` (propagates to `GlobalExceptionHandler`) or `.fold(onSuccess, onFailure)`.
- Map domain exceptions to HTTP responses in `GlobalExceptionHandler` (`shared/exception/`).
- Do not use checked exceptions. Use sealed class hierarchies for known failure modes.

## Mapper Pattern

Mapper extension functions live in the `infrastructure/model/` directory alongside the entity:

```kotlin
// In UserEntity.kt or a companion UserMapper.kt in the same package
fun UserEntity.toDomain(): User = User(id = id, name = name, ...)
fun User.toEntity(): UserEntity = UserEntity(id = id, name = name, ...)
fun User.toDTO(): UserDTO = UserDTO(id = id, name = name, ...)
```

## Multi-Tenancy

- Tenant context is stored in a `ScopedValue` (Java 21+), not `ThreadLocal`.
- Always use `@TenantTransactional` for tenant-DB operations and `@CatalogTransactional` for catalog-DB operations. Never use plain `@Transactional`.
- Never access `TenantContext` from the `domain` or `application` layers. Only infrastructure may interact with it.
- Cross-tenant data access is forbidden by design.

## Security

- All endpoints require authentication by default (`anyRequest().authenticated()` in `SecurityConfig`).
- Use `@PreAuthorize("hasRole('USER')")` (or appropriate role) on controller methods.
- Roles come from the JWT `roles` claim, uppercased and prefixed with `ROLE_`.
- The JWT principal type is `UserInfo` — access via `TenantJwtAuthenticationToken`.

## Testing Conventions

- **Unit tests:** Plain JUnit 5 + Mockito-Kotlin. Test one class in isolation; mock all collaborators.
- **JPA slice tests:** Use `@DataJpaTest` + `@AutoConfigureTestDatabase` with H2 and a `@Configuration` inner class to wire the correct `EntityManagerFactory` and `TransactionManager` bean names.
- **Architecture tests:** Add rules to `ArchitectureTests.kt` using ArchUnit's fluent API.
- Test method names use backtick-quoted sentences: `` `findAll returns empty list when repository is empty` ``.
- Structure tests with `// Arrange`, `// Act`, `// Assert` comments.
- Use AssertJ (`assertThat(...)`) for assertions, not JUnit `assertEquals`.
- Do not use `@SpringBootTest` for unit tests — prefer slice tests or plain unit tests for speed.
- Disable Flyway in slice tests: `properties = ["spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"]`.

## Database Migrations

- Flyway migrations live in two locations:
  - `src/main/resources/db/migration/catalog/` — catalog DB (tenants, config tables)
  - `src/main/resources/db/migration/tenant/` — per-tenant DBs (users, absences, etc.)
- Version format: `V1.NNN__Description_with_underscores.sql` (e.g. `V1.003__Add_user_preferences.sql`).
- Always include a seed `INSERT` for the `default` tenant in catalog migrations where applicable.

## Adding a New Module

1. Create `src/main/kotlin/es/bdo/skeleton/<module>/package-info.java` annotated with `@org.springframework.modulith.ApplicationModule`.
2. Create `domain/`, `application/`, `infrastructure/` sub-packages.
3. Add domain model (`data class`), repository interface, and `UserStatus`-style enum if needed.
4. Add JPA `*Entity`, `*JpaRepository extends CrudRepository`, infrastructure `*Repository` implementation.
5. Add `*DTO`, `*QueryHandler`/`*CommandHandler`, and `*Controller`.
6. Add Flyway migration under the correct location.
7. Update `ArchitectureTests.kt` with the new module's cross-module dependency rules.
8. Consult the agent skills in `.agents/skills/` — especially `spring-boot-crud-patterns` and `test-driven-development`.

## Agent Skills

Specialized skills for this repo live in `.agents/skills/`. Relevant skills:

- `spring-boot-crud-patterns` — CRUD workflows for new modules
- `spring-boot-rest-api-standards` — REST endpoint conventions
- `spring-boot-test-patterns` — test structure and slice test setup
- `spring-boot-security-jwt` — JWT/auth patterns
- `test-driven-development` — Red-Green-Refactor workflow
- `systematic-debugging` — debugging process before proposing fixes
- `verification-before-completion` — must verify before claiming work is done
