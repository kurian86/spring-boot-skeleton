package es.bdo.skeleton.user.infrastructure.controller

import es.bdo.skeleton.shared.model.PaginationResult
import es.bdo.skeleton.user.application.model.UserDTO
import es.bdo.skeleton.user.application.query.GetAllUserQuery
import es.bdo.skeleton.user.application.query.GetAllUserQueryHandler
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(
    private val getAllUserQueryHandler: GetAllUserQueryHandler,
) {

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    fun index(): PaginationResult<UserDTO> {
        return getAllUserQueryHandler.handle(GetAllUserQuery())
            .getOrThrow()
    }
}
