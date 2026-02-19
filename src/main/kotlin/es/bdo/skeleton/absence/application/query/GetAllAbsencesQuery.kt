package es.bdo.skeleton.absence.application.query

import es.bdo.skeleton.shared.cqrs.ListQuery
import es.bdo.skeleton.shared.model.FilterGroup
import es.bdo.skeleton.shared.model.Sort
import es.bdo.skeleton.shared.request.OffsetLimitPageable
import org.springframework.data.domain.Pageable

data class GetAllAbsencesQuery(
    override val pageable: Pageable = OffsetLimitPageable(0),
    override val sort: Sort? = null,
    override val filters: List<FilterGroup> = emptyList()
) : ListQuery
