# Project Architecture

This document outlines the architectural rules of the project, based on **Clean Architecture** (also known as Hexagonal Architecture or Ports & Adapters) with a **Modular Monolith** approach.

## Table of Contents

- [Architectural Principles](#architectural-principles)
- [Module Structure](#module-structure)
- [Architecture Layers](#architecture-layers)
- [Dependency Rules](#dependency-rules)
- [Inter-Module Communication](#inter-module-communication)
- [CQRS Pattern](#cqrs-pattern)
- [Caching Strategy](#caching-strategy)
- [DTOs and Mappers](#dtos-and-mappers)
- [Architectural Testing](#architectural-testing)

---

## Architectural Principles

### 1. Clean Architecture
- Dependencies point inwards (towards the domain)
- The domain layer is unaware of external layers
- Infrastructure depends on domain, never the other way around

### 2. Modular Monolith
- Independent modules with well-defined boundaries
- Each module may evolve independently
- Modules can eventually be extracted as microservices

### 3. Domain-Driven Design (DDD)
- Rich domain model with business logic
- Well-defined aggregates
- Ubiquitous language in each module

---

## Module Structure

```
es.bdo.skeleton/
├── user/              # User management module
├── tenant/            # Multi-tenancy and configuration module
├── absence/           # Absence management module
└── shared/            # Shared Kernel (cross-cutting helpers)
```

Each module follows this structure:

```
<module>/
├── domain/            # Domain layer (models, interfaces)
├── application/       # Application layer (use cases, DTOs, providers)
└── infrastructure/    # Infrastructure layer (controllers, repositories, JPA)
```

---

## Architecture Layers

### Domain Layer

**Location**: `<module>/domain/`

**Responsibilities:**
- Define domain models (entities, value objects)
- Define repository interfaces (ports)
- Contain pure business logic
- NO external dependencies

**Example:**
```kotlin
// user/domain/User.kt
data class User(
    val id: UUID,
    val name: String,
    val email: String,
    val status: UserStatus = UserStatus.ACTIVE
)

// user/domain/UserRepository.kt
interface UserRepository {
    fun findAll(): List<User>
    fun findByEmail(email: String): User?
    fun save(user: User): User
}
```

**Rules:**
- ✅ No Spring annotations
- ✅ No dependencies on other modules
- ✅ No dependencies on application or infrastructure
- ❌ Do NOT use classes from other layers

---

### Application Layer

**Location**: `<module>/application/`

**Responsibilities:**
- Implement use cases
- Define DTOs for communication between modules
- Expose Providers as public facades for the module
- Orchestrate domain operations

**Structure:**
```
application/
├── model/          # DTOs and mappers
│   ├── UserDTO.kt
│   └── UserMapper.kt (extensions: toDTO(), toDomain())
├── command/        # Commands for write operations
│   ├── RegisterUserCommand.kt       # Data class with input parameters
│   ├── RegisterUserCommandHandler.kt # Handler implementation
│   └── UpdateUserCommandHandler.kt
├── query/          # Queries for read operations
│   ├── GetAllUsersQuery.kt          # Data class with filter parameters
│   ├── GetAllUsersQueryHandler.kt   # Handler implementation
│   └── GetUserByIdQueryHandler.kt
├── provider/       # Providers (optional, only if needed by other modules)
│   └── UserProvider.kt
└── exception/      # Business exceptions
    └── UserAlreadyExistsException.kt
```

**Command Example:**
```kotlin
// user/application/command/RegisterUserCommand.kt
data class RegisterUserCommand(
    val username: String,
    val email: String
)

// user/application/command/RegisterUserCommandHandler.kt
@Service
class RegisterUserCommandHandler(
    private val repository: UserRepository,          // Same module
    private val configProvider: ConfigProvider       // Another module (tenant)
) : CommandHandler<RegisterUserCommand, UserDTO> {
    
    @Transactional
    override fun handle(command: RegisterUserCommand): Result<UserDTO> {
        return runCatching {
            // 1. Work with domain models internally
            val existingUser = repository.findByEmail(command.email)
            if (existingUser != null) {
                throw UserAlreadyExistsException("...")
            }
            
            // 2. Use DTOs from other modules
            val tenantConfig = configProvider.find()
                ?: throw TenantNotConfiguredException("...")
            
            // 3. Create and save domain model
            val newUser = User(
                UUID.randomUUID(),
                command.username,
                command.email
            )
            
            // 4. Return DTO wrapped in Result
            repository.save(newUser).toDTO()
        }
    }
}
```

**Query Example:**
```kotlin
// user/application/query/GetAllUsersQuery.kt
data class GetAllUsersQuery(
    val page: Int = 0,
    val size: Int = 20
)

// user/application/query/GetAllUsersQueryHandler.kt
@Service
class GetAllUsersQueryHandler(
    private val repository: UserRepository          // Same module
) : QueryHandler<GetAllUsersQuery, PaginationResult<UserDTO>> {
    
    @Transactional(readOnly = true)
    override fun handle(query: GetAllUsersQuery): Result<PaginationResult<UserDTO>> {
        return runCatching {
            val total = repository.count()
            val items = repository.findAll()
                .map { it.toDTO() }
            
            PaginationResult.from(total, items)
        }
    }
}
```

**Provider Example:**
```kotlin
@Service
class ConfigProvider(
    private val repository: ConfigRepository
) {
    
    fun findByTenantId(tenantId: String): ConfigDTO? {
        return repository.findByTenantId(tenantId)?.toDTO()
    }
}
```

**Rules:**
- ✅ Command Handlers inject Repository from the SAME module
- ✅ Command Handlers inject Providers from OTHER modules
- ✅ Command Handlers return `Result<DTO>` (NEVER domain models) after write operations
- ✅ Query Handlers inject Repository from the SAME module
- ✅ Query Handlers return `Result<DTO>` (NEVER domain models) for read operations
- ✅ Providers return DTOs (NEVER domain models)
- ✅ Providers use Repository internally
- ❌ Command Handlers do NOT inject Providers from their own module
- ❌ Application must NOT depend on infrastructure

---

### Infrastructure Layer

**Location**: `<module>/infrastructure/`

**Responsibilities:**
- Implement infrastructure adapters
- Implement repository implementations (persistence adapters)
- Implement REST controllers
- Manage JPA persistence

**Structure:**
```
infrastructure/
├── controller/     # REST controllers
│   └── UserController.kt
├── model/          # JPA entities and mappers
│   ├── UserEntity.kt
│   └── UserMapper.kt (extensions: toDomain(), toEntity())
├── repository/     # Repository implementations
│   ├── UserRepository.kt (implements domain.UserRepository)
│   └── UserJpaRepository.kt (Spring Data JPA)
└── config/         # Module-specific configs
```

**Repository Implementation Example:**
```kotlin
@Repository
class UserRepository(
    private val jpaRepository: UserJpaRepository
) : IUserRepository { // IUserRepository from domain
    
    override fun findAll(): List<User> {
        return jpaRepository.findAll()
            .map { it.toDomain() }
    }
    
    override fun findByEmail(email: String): User? {
        return jpaRepository.findByEmail(email)
            .map { it.toDomain() }
            .orElse(null)
    }
    
    override fun save(user: User): User {
        val entity = user.toEntity()
        val savedEntity = jpaRepository.save(entity)
        return savedEntity.toDomain()
    }
}
```

**Controller Example:**
```kotlin
@RestController
@RequestMapping("/api/users")
class UserController(
    private val getAllUsersQueryHandler: GetAllUsersQueryHandler,
    private val registerUserCommandHandler: RegisterUserCommandHandler
) {
    
    @GetMapping
    fun index(): ResponseEntity<PaginationResult<UserDTO>> {
        val query = GetAllUsersQuery(page = 0, size = 20)
        return getAllUsersQueryHandler.handle(query)
            .fold(
                onSuccess = { ResponseEntity.ok(it) },
                onFailure = { throw it }
            )
    }
    
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@RequestBody request: RegisterUserRequest): ResponseEntity<UserDTO> {
        val command = RegisterUserCommand(
            username = request.username,
            email = request.email
        )
        return registerUserCommandHandler.handle(command)
            .fold(
                onSuccess = { ResponseEntity.status(HttpStatus.CREATED).body(it) },
                onFailure = { throw it }
            )
    }
}
```

**Rules:**
- ✅ Controllers inject Command Handlers and Query Handlers (never Repositories directly)
- ✅ Repository implementations use JPA internally
- ✅ Infrastructure can depend on domain and application
- ❌ Infrastructure MUST NOT expose domain models in public APIs

---

## Dependency Rules

### Rule 1: Intra-Module Dependencies

```
Infrastructure → Application → Domain
```

- **Domain** depends on NO other layer
- **Application** only depends on Domain
- **Infrastructure** may depend on Domain and Application

### Rule 2: Inter-Module Dependencies

**ALLOWED:**
```kotlin
// User module can use application from tenant module
import es.bdo.skeleton.tenant.application.ConfigProvider
import es.bdo.skeleton.tenant.application.model.ConfigDTO
```

**FORBIDDEN:**
```kotlin
// ❌ User module CANNOT use domain from tenant module
import es.bdo.skeleton.tenant.domain.Config

// ❌ User module CANNOT use infrastructure from tenant module
import es.bdo.skeleton.tenant.infrastructure.ConfigRepository
```

### Rule 3: Shared Kernel

```kotlin
// ✅ All modules can access shared
import es.bdo.skeleton.shared.exception.DomainException
import es.bdo.skeleton.shared.util.DateUtils
```

---

## Inter-Module Communication

### Within a Module

```
Controller → Command/Query Handler → Repository → Database
              ↓
           Domain Models
              ↓
            DTO (for exposure)
```

**Query Example:**
```kotlin
// user/application/query/GetAllUsersQueryHandler.kt
@Service
class GetAllUsersQueryHandler(
    private val repository: UserRepository // SAME module
) : QueryHandler<GetAllUsersQuery, PaginationResult<UserDTO>> {
    
    @Transactional(readOnly = true)
    override fun handle(query: GetAllUsersQuery): Result<PaginationResult<UserDTO>> {
        return runCatching {
            val total = repository.count()
            val items = repository.findAll()        // Returns List<User> (domain)
                .map { it.toDTO() }                 // Converts to List<UserDTO>
            
            PaginationResult.from(total, items)
        }
    }
}

### Between Modules

```
User Module                      Tenant Module
-----------                      -------------
Command Handler → ConfigProvider → ConfigRepository
            ↓                      ↓
         ConfigDTO              Config (domain)
```

**Example:**
```kotlin
// user/application/command/RegisterUserCommandHandler.kt
@Service
class RegisterUserCommandHandler(
    private val configProvider: ConfigProvider, // Provider from another module
    private val repository: UserRepository      // Repository from same module
) : CommandHandler<RegisterUserCommand, UserDTO> {
    
    override fun handle(command: RegisterUserCommand): Result<UserDTO> {
        return runCatching {
            val config = configProvider.find() // Receives ConfigDTO
            
            // ... business logic with config (DTO) and user (domain)
            
            savedUser.toDTO() // Returns UserDTO
        }
    }
}

### Communication Matrix

| From → To | Domain (same) | Application (same) | Infrastructure (same) | Application (other module) | Domain/Infra (other module) |
|-----------|---------------|--------------------|-----------------------|----------------------------|-----------------------------|
| **Domain**       | ✅ | ❌ | ❌ | ❌ | ❌ |
| **Application**  | ✅ | ✅ | ❌ | ✅ (Provider + DTO) | ❌ |
| **Infrastructure** | ✅ | ✅ | ✅ | ✅ | ❌ |

---

## CQRS Pattern

We follow the **Command Query Responsibility Segregation (CQRS)** pattern to separate read and write operations.

### Commands (Write Operations)

**Location**: `<module>/application/command/`

**Structure:**
- `<Action><Entity>Command.kt` - Data class with input parameters
- `<Action><Entity>CommandHandler.kt` - Handler implementing `CommandHandler<T, R>`

**Responsibilities:**
- Handle all write operations (create, update, delete)
- Contain business logic and validation
- Return `Result<DTO>` after write operations
- Always use `@Transactional`

**Naming Convention:**
- `Create<Entity>Command` / `Create<Entity>CommandHandler`
- `Update<Entity>Command` / `Update<Entity>CommandHandler`
- `Delete<Entity>Command` / `Delete<Entity>CommandHandler`
- `Perform<Action>Command` / `Perform<Action>CommandHandler`

**Example:**
```kotlin
// product/application/command/CreateProductCommand.kt
data class CreateProductCommand(
    val name: String,
    val price: BigDecimal
)

// product/application/command/CreateProductCommandHandler.kt
@Service
class CreateProductCommandHandler(
    private val repository: ProductRepository
) : CommandHandler<CreateProductCommand, ProductDTO> {
    
    @Transactional
    override fun handle(command: CreateProductCommand): Result<ProductDTO> {
        return runCatching {
            // Business logic
            val product = Product(
                id = UUID.randomUUID(),
                name = command.name,
                price = command.price
            )
            repository.save(product).toDTO()
        }
    }
}
```

### Queries (Read Operations)

**Location**: `<module>/application/query/`

**Structure:**
- `Get<Entity>ByIdQuery.kt` - Data class with filter parameters
- `Get<Entity>ByIdQueryHandler.kt` - Handler implementing `QueryHandler<T, R>`

**Responsibilities:**
- Handle all read operations
- Return `Result<DTO>` or `Result<PaginationResult<DTO>>`
- No side effects (read-only)
- Use `@Transactional(readOnly = true)`

**Naming Convention:**
- `Get<Entity>ByIdQuery` / `Get<Entity>ByIdQueryHandler`
- `GetAll<Entities>Query` / `GetAll<Entities>QueryHandler`
- `Search<Entities>Query` / `Search<Entities>QueryHandler`
- `Count<Entities>Query` / `Count<Entities>QueryHandler`

**Example:**
```kotlin
// product/application/query/GetProductByIdQuery.kt
data class GetProductByIdQuery(
    val id: UUID
)

// product/application/query/GetProductByIdQueryHandler.kt
@Service
class GetProductByIdQueryHandler(
    private val repository: ProductRepository
) : QueryHandler<GetProductByIdQuery, ProductDTO> {
    
    @Transactional(readOnly = true)
    override fun handle(query: GetProductByIdQuery): Result<ProductDTO> {
        return runCatching {
            repository.findById(query.id)?.toDTO()
                ?: throw ProductNotFoundException("Product ${query.id} not found")
        }
    }
}
```

### Communication Flow

```
┌─────────────────┐
│   Controller    │
└───────┬─────────┘
        │
   ┌────┴────┐
   │         │
   ▼         ▼
Command    Query
Handler    Handler
   │         │
   ▼         ▼
Repository  Repository
   │         │
   ▼         ▼
  Domain    Domain
   │         │
   ▼         ▼
Result<DTO> Result<DTO>
```

**Benefits:**
- **Clarity**: Clear separation between reads and writes
- **Optimization**: Queries can be optimized separately from commands
- **Scalability**: Read and write operations can scale independently
- **Testability**: Easier to test individual operations
- **Error Handling**: `Result<T>` provides functional error handling with `runCatching`

---

## Caching Strategy

### Principle: Cache close to the data

Caching should occur in the **Infrastructure** (Repository) layer, not in Application.

### Where to apply @Cacheable

| Scenario                         | Location                | What to cache     |
|----------------------------------|-------------------------|-------------------|
| Simple queries (findById, etc.)  | Repository (Infrastructure) | Domain models   |
| Complex queries / Reports        | QueryService (Application)  | Computed results|
| DTOs for other modules           | Provider (Application)      | DTOs            |

**Repository Example:**
```kotlin
// tenant/infrastructure/TenantRepository.kt
@Repository
class TenantRepository(
    private val jpaRepository: TenantJpaRepository
) : ITenantRepository {
    
    @Cacheable(value = ["tenants"], key = "#tenantId")
    override fun findById(tenantId: String): Tenant? {
        return jpaRepository.findById(tenantId)
            .map { it.toDomain() }
            .orElse(null)
    }
    
    @CacheEvict(value = ["tenants"], key = "#tenantId")
    override fun evictCache(tenantId: String) {
    }
    
    @CacheEvict(value = ["tenants"], allEntries = true)
    override fun evictAll() {
    }
}
```

**Provider Example (optional):**
```kotlin
// tenant/application/ConfigProvider.kt
@Service
class ConfigProvider(
    private val repository: ConfigRepository
) {
    
    @Cacheable(value = ["config-dtos"], key = "#tenantId")
    fun findByTenantId(tenantId: String): ConfigDTO? {
        return repository.findByTenantId(tenantId)?.toDTO()
    }
}
```

**Cache Rules:**
- ✅ `@Cacheable` on read methods
- ✅ `@CacheEvict` on cache invalidation methods
- ✅ `@CachePut` to update cache after modifications
- ❌ Do NOT use `@Cacheable` on `evict*` methods (use `@CacheEvict` instead)
- ❌ Do NOT cache Query Handlers or Command Handlers (too high level)

---

## DTOs and Mappers

### DTO Location

```
<module>/application/model/
├── UserDTO.kt
└── UserMapper.kt (or extensions in the same file)
```

**DTO Structure Example:**
```kotlin
package es.bdo.skeleton.user.application.model

import es.bdo.skeleton.user.domain.User
import java.util.UUID

data class UserDTO(
    val id: UUID,
    val name: String,
    val email: String,
    val status: UserStatus
)

// Extension function mappers
fun User.toDTO() = UserDTO(
    id = id,
    name = name,
    email = email,
    status = status
)

fun UserDTO.toDomain() = User(
    id = id,
    name = name,
    email = email,
    status = status
)
```

**DTO Rules:**
- ✅ DTOs live under `application/model/`
- ✅ DTOs are immutable data classes
- ✅ Mappers as extension functions (`toDTO()`, `toDomain()`)
- ✅ DTOs may import domain models for mapping
- ✅ Domain models NEVER import DTOs
- ❌ DTOs must NOT contain business logic
- ❌ DTOs should NOT be directly exposed in REST APIs (use Views in `infrastructure/model/view`)

**Entity Mappers (infrastructure):**
```kotlin
// user/infrastructure/model/UserEntity.kt
@Entity
@Table(name = "users")
data class UserEntity(
    @Id val id: UUID,
    val name: String,
    val email: String,
    // ... more JPA fields
)

// Mappers
fun UserEntity.toDomain() = User(
    id = id,
    name = name,
    email = email
)

fun User.toEntity() = UserEntity(
    id = id,
    name = name,
    email = email
)
```

---

## Architectural Testing

### ArchUnit Tests

The project uses ArchUnit to automatically verify architectural rules.

Location: `src/test/kotlin/es/bdo/skeleton/ArchitectureTests.kt`

### Rules Verified

#### 1. Modules only access other modules via application

```kotlin
@Test
fun `Modules can only access other modules through application layer`() {
    // User module CANNOT access tenant.domain or tenant.infrastructure
    noClasses()
        .that().resideInAPackage("..user..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..tenant.domain..", "..tenant.infrastructure..")
        .check(classes)
}
```

#### 2. Domain does not depend on other layers
```kotlin
@Test
fun `Domain layer should not depend on other layers`() {
    layeredArchitecture()
        .layer("Domain").definedBy("..domain..")
        .layer("Application").definedBy("..application..")
        .layer("Infrastructure").definedBy("..infrastructure..")
        .whereLayer("Domain").mayNotAccessAnyLayer()
        .check(classes)
}
```

#### 3. Application only depends on Domain
```kotlin
@Test
fun `Application layer should only depend on domain`() {
    layeredArchitecture()
        .layer("Domain").definedBy("..domain..")
        .layer("Application").definedBy("..application..")
        .layer("Infrastructure").definedBy("..infrastructure..")
        .whereLayer("Application").mayOnlyAccessLayers("Domain")
        .check(classes)
}
```

#### 4. Infrastructure may depend on Domain and Application
```kotlin
@Test
fun `Infrastructure layer can depend on domain and application`() {
    layeredArchitecture()
        .layer("Domain").definedBy("..domain..")
        .layer("Application").definedBy("..application..")
        .layer("Infrastructure").definedBy("..infrastructure..")
        .whereLayer("Infrastructure").mayOnlyAccessLayers("Domain", "Application")
        .check(classes)
}
```

### Run Architecture Tests
```bash
./gradlew test --tests ArchitectureTests
```
---

## Development Checklist

When creating a new feature, verify:

### Domain
- [ ] Domain models without Spring annotations
- [ ] Repository interfaces without implementation
- [ ] Business logic in domain
- [ ] No external dependencies

### Application
- [ ] DTOs in `application/model/`
- [ ] Extension function mappers
- [ ] Command Handlers implement `CommandHandler<T, R>` interface
- [ ] Command Handlers inject Repository from SAME module
- [ ] Command Handlers inject Providers from OTHER modules
- [ ] Command Handlers return `Result<DTO>` after write operations
- [ ] Query Handlers implement `QueryHandler<T, R>` interface
- [ ] Query Handlers inject Repository from SAME module
- [ ] Query Handlers return `Result<DTO>` for read operations
- [ ] Providers return DTOs (if exposed to other modules)

### Infrastructure
- [ ] Repository implementation under `infrastructure/`
- [ ] JPA Entity under `infrastructure/model/`
- [ ] Controllers inject Use Cases
- [ ] Controllers return DTOs or Views
- [ ] Caching in Repository if needed

### Testing
- [ ] Command Handler unit tests
- [ ] Query Handler unit tests
- [ ] Repository integration tests
- [ ] Controller slice tests
- [ ] All ArchUnit tests pass

---

## Patterns and Best Practices

### 1. CQRS Pattern (Command Query Responsibility Segregation)
- **Commands**: Handle write operations (create, update, delete)
  - One Command = One write operation
  - Input: `Command` data class
  - Output: `Result<DTO>`
  - Methods: `handle(command)`
  - Interface: `CommandHandler<T, R>`
- **Queries**: Handle read operations
  - One Query = One read operation
  - Input: `Query` data class with filter/sort parameters
  - Output: `Result<DTO>` or `Result<PaginationResult<DTO>>`
  - Methods: `handle(query)`
  - Interface: `QueryHandler<T, R>`

### 2. Repository Pattern
- Interface in Domain
- Implementation in Infrastructure
- Works with domain models
- Responsible for persistence/retrieval

### 3. Provider Pattern (Anti-Corruption Layer)
- Public facade of the module
- Returns DTOs, never domain models
- Uses Repository internally
- Protects domain from other modules

### 4. Mapper Pattern
- Extension functions for conversion
- `toDTO()`: Domain → DTO
- `toDomain()`: DTO → Domain
- `toEntity()`: Domain → Entity (JPA)
- `toDomain()`: Entity → Domain

---

## Complete Examples

### Example 1: Creating a new module
```
product/
├── domain/
│   ├── Product.kt
│   └── ProductRepository.kt
├── application/
│   ├── model/
│   │   └── ProductDTO.kt
│   ├── command/
│   │   ├── CreateProductCommand.kt         # Data class
│   │   ├── CreateProductCommandHandler.kt  # Handler
│   │   ├── UpdateProductCommand.kt         # Data class
│   │   └── UpdateProductCommandHandler.kt  # Handler
│   ├── query/
│   │   ├── GetAllProductsQuery.kt          # Data class
│   │   ├── GetAllProductsQueryHandler.kt   # Handler
│   │   ├── GetProductByIdQuery.kt          # Data class
│   │   └── GetProductByIdQueryHandler.kt   # Handler
│   └── ProductProvider.kt (if exposed to other modules)
└── infrastructure/
    ├── controller/
    │   └── ProductController.kt
    ├── model/
    │   ├── ProductEntity.kt
    │   └── ProductMapper.kt
    └── ProductRepository.kt
```

### Example 2: Command Handler that consumes another module
```kotlin
// order/application/command/CreateOrderCommand.kt
data class CreateOrderCommand(
    val productId: UUID,
    val userId: UUID,
    val quantity: Int
)

// order/application/command/CreateOrderCommandHandler.kt
@Service
class CreateOrderCommandHandler(
    private val orderRepository: OrderRepository,    // Same module
    private val productProvider: ProductProvider,    // Another module (product)
    private val userProvider: UserProvider           // Another module (user)
) : CommandHandler<CreateOrderCommand, OrderDTO> {
    
    @Transactional
    override fun handle(command: CreateOrderCommand): Result<OrderDTO> {
        return runCatching {
            // Validate that the product exists (uses DTO from another module)
            val product = productProvider.findById(command.productId)
                ?: throw ProductNotFoundException()
            
            // Validate that the user exists (uses DTO from another module)
            val user = userProvider.findById(command.userId)
                ?: throw UserNotFoundException()
            
            // Create order (domain model from this module)
            val order = Order(
                id = UUID.randomUUID(),
                productId = command.productId,
                userId = command.userId,
                quantity = command.quantity,
                totalPrice = product.price * command.quantity
            )
            
            orderRepository.save(order).toDTO()
        }
    }
}
```
---

## FAQ

### Why shouldn't Command Handlers use Providers from their own module?
Because the Provider is an **anti-corruption layer** for other modules. Within the same module, the Command Handler should work directly with the domain through the Repository. Adding a Provider in the middle would be redundant and add unnecessary Domain ↔ DTO conversions.

### When should I create a Provider?
Only if **other modules need to access your module**. If your module is self-contained and doesn't expose functionalities for other modules, you do not need a Provider.

### Where should caching go?
- **Repository**: To cache domain data (findById, findAll)
- **Provider**: To cache DTOs shared with other modules
- **QueryService**: To cache complex query or report results
**Never** in Command Handlers or Query Handlers, since they can have very specific logic that shouldn't be cached.

### Can DTOs have logic?
No. DTOs are **data transfer objects**, and must NOT contain business logic. Logic should be in the domain or Use Cases.

### Can I expose DTOs directly in Controllers?
Yes, but it's better to create **Views** (API Response DTOs) in `infrastructure/model/view/`. This gives you flexibility to change the API representation without affecting module-to-module DTO contracts.

---

## Conclusion

This architecture provides:

- **Modularity**: Independent, decoupled modules
- **Testability**: Well-defined layers easy to test
- **Maintainability**: Localized changes with minimal side effects
- **Scalability**: Modules easy to extract as microservices
- **Clarity**: Explicit and automatically verifiable rules

Keep these rules in mind while developing, and verify with ArchUnit tests.
