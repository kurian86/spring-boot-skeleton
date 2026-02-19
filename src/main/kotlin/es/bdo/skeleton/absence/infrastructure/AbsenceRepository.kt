package es.bdo.skeleton.absence.infrastructure

import es.bdo.skeleton.absence.domain.Absence
import es.bdo.skeleton.absence.infrastructure.model.toDomain
import es.bdo.skeleton.shared.model.FilterGroup
import es.bdo.skeleton.shared.model.Sort
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import es.bdo.skeleton.absence.domain.AbsenceRepository as IAbsenceRepository
import org.springframework.data.domain.Sort as SpringSort

@Repository
class AbsenceRepository(
    private val jpaRepository: AbsenceJpaRepository
) : IAbsenceRepository {

    override fun findAll(
        offset: Long,
        limit: Int,
        sort: Sort?,
        filters: List<FilterGroup>
    ): Pair<Long, List<Absence>> {
        val pageable = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            buildSpringSort(sort)
        )

        return jpaRepository.findAll(pageable)
            .let { page -> page.totalElements to page.content.map { it.toDomain() } }
    }

    private fun buildSpringSort(sort: Sort?): SpringSort {
        if (sort == null) return SpringSort.unsorted()
        return SpringSort.by(sort.direction, sort.property)
    }
}
