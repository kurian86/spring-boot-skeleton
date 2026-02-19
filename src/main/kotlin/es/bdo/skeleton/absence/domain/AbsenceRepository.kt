package es.bdo.skeleton.absence.domain

import es.bdo.skeleton.shared.model.FilterGroup
import es.bdo.skeleton.shared.model.Sort

interface AbsenceRepository {
    fun count(filters: List<FilterGroup> = emptyList()): Long

    fun findAll(
        offset: Long = 0,
        limit: Int = 10,
        sort: Sort? = null,
        filters: List<FilterGroup> = emptyList()
    ): List<Absence>
}
