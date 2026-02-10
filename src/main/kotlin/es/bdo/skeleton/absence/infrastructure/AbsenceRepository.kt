package es.bdo.skeleton.absence.infrastructure

import es.bdo.skeleton.absence.domain.Absence
import es.bdo.skeleton.absence.domain.IAbsenceRepository
import es.bdo.skeleton.absence.infrastructure.model.toDomain
import org.springframework.stereotype.Repository

@Repository
class AbsenceRepository(
    private val jpaRepository: AbsenceJpaRepository
) : IAbsenceRepository {

    override fun findAll(): List<Absence> {
        return jpaRepository.findAll()
            .map { it.toDomain() }
    }
}
