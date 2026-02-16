package es.bdo.skeleton.absence.application.query

import es.bdo.skeleton.absence.application.model.AbsenceDTO
import es.bdo.skeleton.absence.application.model.toDTO
import es.bdo.skeleton.absence.domain.AbsenceRepository
import es.bdo.skeleton.shared.cqrs.QueryHandler
import es.bdo.skeleton.shared.model.PaginationResult
import org.springframework.stereotype.Service

@Service
class GetAllAbsencesQueryHandler(
    private val repository: AbsenceRepository
) : QueryHandler<GetAllAbsencesQuery, PaginationResult<AbsenceDTO>> {

    override fun handle(query: GetAllAbsencesQuery): Result<PaginationResult<AbsenceDTO>> {
        return runCatching {
            val total = repository.count()
            val items = repository.findAll().map { it.toDTO() }

            PaginationResult(total, items)
        }
    }
}
