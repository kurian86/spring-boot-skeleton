package es.bdo.skeleton.absence.application.usecase

import es.bdo.skeleton.absence.application.model.AbsenceDTO
import es.bdo.skeleton.absence.application.model.toDTO
import es.bdo.skeleton.absence.domain.AbsenceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetAllAbsenceUseCase(
    private val repository: AbsenceRepository
) {

    @Transactional(readOnly = true)
    fun handle(): List<AbsenceDTO> {
        return repository.findAll()
            .map { it.toDTO() }
    }
}
