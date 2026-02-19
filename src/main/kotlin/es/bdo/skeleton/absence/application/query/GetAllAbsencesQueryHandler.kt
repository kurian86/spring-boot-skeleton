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
            val offset = query.pageable.offset
            val limit = query.pageable.pageSize
            val sort = query.sort
            val filters = query.filters

            val (total, items) = repository.findAll(offset, limit, sort, filters)
                .let { (total, absences) -> total to absences.map { it.toDTO() } }

            PaginationResult.from(
                totalCount = total,
                items = items,
                offset = offset,
                limit = limit
            )
        }
    }
}
