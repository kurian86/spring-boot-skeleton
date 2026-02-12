package es.bdo.skeleton.absence.application.usecase

import es.bdo.skeleton.absence.application.AbsenceProvider
import es.bdo.skeleton.absence.domain.Absence
import org.springframework.stereotype.Service

@Service
class GetAllAbsenceUseCase(
    private val provider: AbsenceProvider
) {
    fun handle(): List<Absence> {
        return provider.findAll()
    }
}
