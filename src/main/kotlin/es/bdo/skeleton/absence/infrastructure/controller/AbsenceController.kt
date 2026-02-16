package es.bdo.skeleton.absence.infrastructure.controller

import es.bdo.skeleton.absence.application.model.AbsenceDTO
import es.bdo.skeleton.absence.application.query.GetAllAbsencesQuery
import es.bdo.skeleton.absence.application.query.GetAllAbsencesQueryHandler
import es.bdo.skeleton.shared.model.PaginationResult
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/absences")
class AbsenceController(
    private val getAllAbsencesQueryHandler: GetAllAbsencesQueryHandler
) {

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    fun index(): PaginationResult<AbsenceDTO> {
        return getAllAbsencesQueryHandler.handle(GetAllAbsencesQuery())
            .getOrThrow()
    }
}
