package es.bdo.skeleton.absence.application

import es.bdo.skeleton.absence.application.model.AbsenceDTO
import es.bdo.skeleton.absence.application.model.toDTO
import es.bdo.skeleton.absence.domain.AbsenceRepository
import org.springframework.stereotype.Service

@Service
class AbsenceProvider(
    private val absenceRepository: AbsenceRepository
) {

    fun findAll(): List<AbsenceDTO> {
        return absenceRepository.findAll(0, 10, null, emptyList())
            .map { it.toDTO() }
    }
}
