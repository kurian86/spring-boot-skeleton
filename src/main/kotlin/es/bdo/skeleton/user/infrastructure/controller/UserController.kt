package es.bdo.skeleton.user.infrastructure.controller

import es.bdo.skeleton.shared.model.FilterGroup
import es.bdo.skeleton.shared.model.PaginationResult
import es.bdo.skeleton.shared.model.Sort
import es.bdo.skeleton.shared.request.OffsetLimitPageable
import es.bdo.skeleton.user.application.model.UserDTO
import es.bdo.skeleton.user.application.query.GetAllUserQuery
import es.bdo.skeleton.user.application.query.GetAllUserQueryHandler
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(
    private val getAllUserQueryHandler: GetAllUserQueryHandler,
) {

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    fun index(
        @RequestParam(required = false, defaultValue = "0") offset: Long,
        @RequestParam(required = false, defaultValue = "10") limit: Int,
        @RequestParam(required = false) sort: Sort?,
        @RequestParam(required = false) filters: Array<FilterGroup>?,
    ): PaginationResult<UserDTO> {
        // Validate parameters
        val validOffset = if (offset < 0) 0 else offset
        val validLimit = if (limit < 1) 10 else if (limit > 100) 100 else limit

        val query = GetAllUserQuery(
            pageable = OffsetLimitPageable(offset = validOffset, limit = validLimit),
            sort = sort,
            filters = filters?.toList() ?: emptyList()
        )

        return getAllUserQueryHandler.handle(query)
            .getOrThrow()
    }
}
