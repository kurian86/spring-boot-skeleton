package es.bdo.skeleton.shared.cqrs

import es.bdo.skeleton.shared.model.FilterGroup
import es.bdo.skeleton.shared.model.Sort
import org.springframework.data.domain.Pageable

interface ListQuery {
    val pageable: Pageable
    val sort: Sort?
    val filters: List<FilterGroup>
}
