package es.bdo.skeleton.user.infrastructure.controller

import es.bdo.skeleton.shared.model.PaginationResult
import es.bdo.skeleton.tenant.application.security.UserInfo
import es.bdo.skeleton.user.application.command.RegisterUserCommand
import es.bdo.skeleton.user.application.command.RegisterUserCommandHandler
import es.bdo.skeleton.user.application.model.UserDTO
import es.bdo.skeleton.user.application.query.GetAllUserQuery
import es.bdo.skeleton.user.application.query.GetAllUserQueryHandler
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val getAllUserQueryHandler: GetAllUserQueryHandler,
    private val registerUserCommandHandler: RegisterUserCommandHandler
) {

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    fun index(): PaginationResult<UserDTO> {
        return getAllUserQueryHandler.handle(GetAllUserQuery())
            .getOrThrow()
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(authentication: Authentication): UserDTO? {
        val userInfo = authentication.principal as UserInfo

        return registerUserCommandHandler.handle(
            RegisterUserCommand(
                userInfo.username,
                userInfo.email
            )
        ).getOrThrow()
    }
}
