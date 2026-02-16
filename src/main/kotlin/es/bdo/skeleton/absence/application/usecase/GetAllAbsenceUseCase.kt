package es.bdo.skeleton.absence.application.usecase

import es.bdo.skeleton.absence.application.model.AbsenceDTO
import es.bdo.skeleton.absence.application.model.toDTO
import es.bdo.skeleton.absence.domain.AbsenceRepository
import es.bdo.skeleton.shared.annotation.TenantTransactional
import org.springframework.stereotype.Service

@Service
class GetAllAbsenceUseCase(
    private val repository: AbsenceRepository
) {

    @TenantTransactional(readOnly = true)
    fun handle(): List<AbsenceDTO> {
        return repository.findAll()
            .map { it.toDTO() }
    }
}
