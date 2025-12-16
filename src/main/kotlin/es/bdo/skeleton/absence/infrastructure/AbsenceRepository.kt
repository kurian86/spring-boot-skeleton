package es.bdo.skeleton.absence.infrastructure

import es.bdo.skeleton.absence.domain.Absence
import es.bdo.skeleton.absence.domain.IAbsenceRepository

class AbsenceRepository : IAbsenceRepository {
    override fun findAll() = listOf<Absence>()
}
