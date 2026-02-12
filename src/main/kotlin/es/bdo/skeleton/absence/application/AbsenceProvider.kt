package es.bdo.skeleton.absence.application

import es.bdo.skeleton.absence.domain.Absence
import es.bdo.skeleton.absence.domain.AbsenceRepository
import org.springframework.stereotype.Service

@Service
class AbsenceProvider(
    private val absenceRepository: AbsenceRepository
) {

    fun findAll(): List<Absence> {
        return absenceRepository.findAll()
    }
}
