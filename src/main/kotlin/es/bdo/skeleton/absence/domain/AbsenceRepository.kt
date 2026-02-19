package es.bdo.skeleton.absence.domain

import es.bdo.skeleton.shared.model.FilterGroup
import es.bdo.skeleton.shared.model.Sort

interface AbsenceRepository {
    fun findAll(
        offset: Long = 0,
        limit: Int = 10,
        sort: Sort? = null,
        filters: List<FilterGroup> = emptyList()
    ): Pair<Long, List<Absence>>
}
