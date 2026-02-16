package es.bdo.skeleton.user.application.command

import es.bdo.skeleton.shared.annotation.TenantTransactional
import es.bdo.skeleton.shared.cqrs.CommandHandler
import es.bdo.skeleton.tenant.application.ConfigProvider
import es.bdo.skeleton.tenant.application.exception.TenantNotConfiguredException
import es.bdo.skeleton.user.application.exception.EmailDomainNotAllowedException
import es.bdo.skeleton.user.application.exception.UserAlreadyExistsException
import es.bdo.skeleton.user.application.model.UserDTO
import es.bdo.skeleton.user.application.model.toDTO
import es.bdo.skeleton.user.domain.User
import es.bdo.skeleton.user.domain.UserRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class RegisterUserCommandHandler(
    private val configProvider: ConfigProvider,
    private val repository: UserRepository
) : CommandHandler<RegisterUserCommand, UserDTO> {

    @TenantTransactional
    override fun handle(command: RegisterUserCommand): Result<UserDTO> {
        return runCatching {
            val existingUser = repository.findByEmail(command.email)
            if (existingUser != null) {
                throw UserAlreadyExistsException("User with email ${command.email} already exists")
            }

            val tenantConfig = configProvider.find()
                ?: throw TenantNotConfiguredException("Tenant is not configured for registration")

            val emailDomain = command.email.substringAfter("@", "")
            if (emailDomain.isEmpty()) {
                throw EmailDomainNotAllowedException("Invalid email format: ${command.email}")
            }

            val isAllowedDomain = tenantConfig.allowedDomains.any { allowedDomain ->
                emailDomain.equals(allowedDomain, ignoreCase = true)
            }

            if (!isAllowedDomain) {
                throw EmailDomainNotAllowedException(
                    "Email domain '$emailDomain' is not allowed for current tenant. " +
                            "Allowed domains: ${tenantConfig.allowedDomains.joinToString(", ")}"
                )
            }

            val newUser = User(
                UUID.randomUUID(),
                command.username,
                command.email
            )

            repository.save(newUser).toDTO()
        }
    }
}
