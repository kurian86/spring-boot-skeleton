# Project Architecture

This document outlines the architectural rules of the project, based on **Clean Architecture** (also known as Hexagonal Architecture or Ports & Adapters) with a **Modular Monolith** approach.

## Table of Contents

- [Architectural Principles](#architectural-principles)
- [Module Structure](#module-structure)
- [Architecture Layers](#architecture-layers)
- [Dependency Rules](#dependency-rules)
- [Inter-Module Communication](#inter-module-communication)
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
├── usecase/        # Use cases
│   ├── RegisterUserUseCase.kt
│   └── GetAllUserUseCase.kt
├── provider/       # Providers (optional, only if needed by other modules)
│   └── UserProvider.kt
└── exception/      # Business exceptions
    └── UserAlreadyExistsException.kt
```

**Use Case Example:**
```kotlin
@Service
class RegisterUserUseCase(
    private val repository: UserRepository,          // Same module
    private val configProvider: ConfigProvider       // Another module (tenant)
) {
    
    @Transactional
    fun handle(params: Params): UserDTO {
        // 1. Work with domain models internally
        val existingUser = repository.findByEmail(params.email)
        if (existingUser != null) {
            throw UserAlreadyExistsException("...")
        }
        
        // 2. Use DTOs from other modules
        val tenantConfig = configProvider.findByTenantId(params.tenantId)
            ?: throw TenantNotConfiguredException("...")
        
        // 3. Create and save domain model
        val newUser = User(
            UUID.randomUUID(),
            params.username,
            params.email
        )
        
        val savedUser = repository.save(newUser)
        
        // 4. Return DTO (never domain model)
        return savedUser.toDTO()
    }
    
    data class Params(...)
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
- ✅ Use Cases inject Repository from the SAME module
- ✅ Use Cases inject Providers from OTHER modules
- ✅ Use Cases return DTOs (NEVER domain models)
- ✅ Providers return DTOs (NEVER domain models)
- ✅ Providers use Repository internally
- ❌ Use Cases do NOT inject Providers from their own module
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
    private val getAllUserUseCase: GetAllUserUseCase,
    private val registerUserUseCase: RegisterUserUseCase
) {
    
    @GetMapping
    fun index(): List<UserDTO> {
        return getAllUserUseCase.handle()
    }
    
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(authentication: Authentication): UserDTO {
        return registerUserUseCase.handle(params)
    }
}
```

**Rules:**
- ✅ Controllers inject Use Cases (never Repositories directly)
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
Controller → Use Case → Repository → Database
              ↓
           Domain Models
              ↓
            DTO (for exposure)
```

**Example:**
```kotlin
// GetAllUserUseCase.kt
@Service
class GetAllUserUseCase(
    private val repository: UserRepository // SAME module
) {
    fun handle(): List<UserDTO> {
        return repository.findAll()        // Returns List<User> (domain)
            .map { it.toDTO() }            // Converts to List<UserDTO>
    }
}
```

### Between Modules

```
User Module                      Tenant Module
-----------                      -------------
Use Case → ConfigProvider → ConfigRepository
             ↓                      ↓
          ConfigDTO              Config (domain)
```

**Example:**
```kotlin
// RegisterUserUseCase.kt (user module)
@Service
class RegisterUserUseCase(
    private val configProvider: ConfigProvider, // Provider from another module
    private val repository: UserRepository      // Repository from same module
) {
    fun handle(params: Params): UserDTO {
        val config = configProvider.findByTenantId(tenantId) // Receives ConfigDTO
        
        // ... business logic with config (DTO) and user (domain)
        
        return savedUser.toDTO() // Returns UserDTO
    }
}
```

### Communication Matrix

| From → To | Domain (same) | Application (same) | Infrastructure (same) | Application (other module) | Domain/Infra (other module) |
|-----------|---------------|--------------------|-----------------------|----------------------------|-----------------------------|
| **Domain**       | ✅ | ❌ | ❌ | ❌ | ❌ |
| **Application**  | ✅ | ✅ | ❌ | ✅ (Provider + DTO) | ❌ |
| **Infrastructure** | ✅ | ✅ | ✅ | ✅ | ❌ |

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
- ❌ Do NOT cache Use Cases (too high level)

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
- [ ] Use Cases inject Repository from SAME module
- [ ] Use Cases inject Providers from OTHER modules
- [ ] Use Cases return DTOs
- [ ] Providers return DTOs (if exposed to other modules)

### Infrastructure
- [ ] Repository implementation under `infrastructure/`
- [ ] JPA Entity under `infrastructure/model/`
- [ ] Controllers inject Use Cases
- [ ] Controllers return DTOs or Views
- [ ] Caching in Repository if needed

### Testing
- [ ] Use Case unit tests
- [ ] Repository integration tests
- [ ] Controller slice tests
- [ ] All ArchUnit tests pass

---

## Patterns and Best Practices

### 1. Use Case Pattern
- One Use Case = One business operation
- Input: `Params` data class
- Output: `DTO` or `List<DTO>`
- Methods: `execute()` or `handle()`

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
│   ├── usecase/
│   │   ├── CreateProductUseCase.kt
│   │   └── GetAllProductsUseCase.kt
│   └── ProductProvider.kt (if exposed to other modules)
└── infrastructure/
    ├── controller/
    │   └── ProductController.kt
    ├── model/
    │   ├── ProductEntity.kt
    │   └── ProductMapper.kt
    └── ProductRepository.kt
```

### Example 2: Use case that consumes another module
```kotlin
// order/application/usecase/CreateOrderUseCase.kt
@Service
class CreateOrderUseCase(
    private val orderRepository: OrderRepository,    // Same module
    private val productProvider: ProductProvider,    // Another module (product)
    private val userProvider: UserProvider           // Another module (user)
) {
    
    @Transactional
    fun handle(params: Params): OrderDTO {
        // Validate that the product exists (uses DTO from another module)
        val product = productProvider.findById(params.productId)
            ?: throw ProductNotFoundException()
        
        // Validate that the user exists (uses DTO from another module)
        val user = userProvider.findById(params.userId)
            ?: throw UserNotFoundException()
        
        // Create order (domain model from this module)
        val order = Order(
            id = UUID.randomUUID(),
            productId = params.productId,
            userId = params.userId,
            quantity = params.quantity,
            totalPrice = product.price * params.quantity
        )
        
        val savedOrder = orderRepository.save(order)
        
        return savedOrder.toDTO()
    }
    
    data class Params(
        val productId: UUID,
        val userId: UUID,
        val quantity: Int
    )
}
```
---

## FAQ

### Why shouldn't Use Cases use Providers from their own module?
Because the Provider is an **anti-corruption layer** for other modules. Within the same module, the Use Case should work directly with the domain through the Repository. Adding a Provider in the middle would be redundant and add unnecessary Domain ↔ DTO conversions.

### When should I create a Provider?
Only if **other modules need to access your module**. If your module is self-contained and doesn't expose functionalities for other modules, you do not need a Provider.

### Where should caching go?
- **Repository**: To cache domain data (findById, findAll)
- **Provider**: To cache DTOs shared with other modules
- **QueryService**: To cache complex query or report results
**Never** in Use Cases, since a Use Case can have very specific logic that shouldn't be cached.

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
