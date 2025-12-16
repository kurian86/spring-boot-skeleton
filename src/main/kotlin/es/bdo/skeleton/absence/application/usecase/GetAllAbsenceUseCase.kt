package es.bdo.skeleton.absence.application.usecase

import es.bdo.skeleton.absence.domain.Absence
import es.bdo.skeleton.absence.domain.IAbsenceRepository
import org.springframework.stereotype.Service

@Service
class GetAllAbsenceUseCase(
    private val repository: IAbsenceRepository
) {
    fun handle(): List<Absence> {
        return repository.findAll()
    }
}
