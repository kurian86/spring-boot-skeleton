package es.bdo.skeleton.user.infrastructure.controller

import es.bdo.skeleton.shared.model.PaginationResult
import es.bdo.skeleton.user.application.model.UserDTO
import es.bdo.skeleton.user.application.query.GetAllUserQuery
import es.bdo.skeleton.user.application.query.GetAllUserQueryHandler
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val getAllUserQueryHandler: GetAllUserQueryHandler,
) {

    @GetMapping
    fun index(): PaginationResult<UserDTO> {
        return getAllUserQueryHandler.handle(GetAllUserQuery())
            .getOrThrow()
    }
}
