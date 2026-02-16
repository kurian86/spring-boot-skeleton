package es.bdo.skeleton.user.infrastructure.controller

import es.bdo.skeleton.tenant.application.TenantContext
import es.bdo.skeleton.tenant.application.exception.TenantNotFoundException
import es.bdo.skeleton.tenant.application.security.TokenValidator
import es.bdo.skeleton.user.application.model.UserDTO
import es.bdo.skeleton.user.application.usecase.GetAllUserUseCase
import es.bdo.skeleton.user.application.usecase.RegisterUserUseCase
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val getAllUserUseCase: GetAllUserUseCase,
    private val registerUserUseCase: RegisterUserUseCase,
    private val tokenValidator: TokenValidator
) {

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    fun index(): List<UserDTO> {
        val users = getAllUserUseCase.handle()
        return users
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(authentication: Authentication): UserDTO? {
        val tenantId = TenantContext.tenantId ?: throw TenantNotFoundException()
        val userInfo = tokenValidator.validateAndExtractUserInfo(authentication)

        return registerUserUseCase.handle(
            RegisterUserUseCase.Params(
                tenantId,
                userInfo.username,
                userInfo.email
            )
        )
    }
}
