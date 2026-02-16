package es.bdo.skeleton.user.application.usecase

import es.bdo.skeleton.tenant.application.ConfigProvider
import es.bdo.skeleton.tenant.application.exception.TenantNotConfiguredException
import es.bdo.skeleton.user.application.exception.EmailDomainNotAllowedException
import es.bdo.skeleton.user.application.exception.UserAlreadyExistsException
import es.bdo.skeleton.user.application.model.UserDTO
import es.bdo.skeleton.user.application.model.toDTO
import es.bdo.skeleton.user.domain.User
import es.bdo.skeleton.user.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class RegisterUserUseCase(
    private val configProvider: ConfigProvider,
    private val repository: UserRepository
) {

    @Transactional
    fun handle(params: Params): UserDTO {
        val existingUser = repository.findByEmail(params.email)
        if (existingUser != null) {
            throw UserAlreadyExistsException("User with email ${params.email} already exists")
        }

        val tenantConfig = configProvider.findByTenantId(params.tenantId)
            ?: throw TenantNotConfiguredException("Tenant ${params.tenantId} is not configured for registration")

        val emailDomain = params.email.substringAfter("@", "")
        if (emailDomain.isEmpty()) {
            throw EmailDomainNotAllowedException("Invalid email format: ${params.email}")
        }

        val isAllowedDomain = tenantConfig.allowedDomains.any { allowedDomain ->
            emailDomain.equals(allowedDomain, ignoreCase = true)
        }

        if (!isAllowedDomain) {
            throw EmailDomainNotAllowedException(
                "Email domain '$emailDomain' is not allowed for tenant ${params.tenantId}. " +
                        "Allowed domains: ${tenantConfig.allowedDomains.joinToString(", ")}"
            )
        }

        val newUser = User(
            UUID.randomUUID(),
            params.username,
            params.email
        )

        return repository.save(newUser).toDTO()
    }

    data class Params(
        val tenantId: String,
        val username: String,
        val email: String
    )
}
