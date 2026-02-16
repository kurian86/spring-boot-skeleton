package es.bdo.skeleton.absence.application.model

import es.bdo.skeleton.absence.domain.Absence
import java.time.LocalDate
import java.util.*

data class AbsenceDTO(
    val id: UUID,
    val userId: UUID,
    val startDate: LocalDate,
    val endDate: LocalDate?
)

fun Absence.toDTO(): AbsenceDTO {
    return AbsenceDTO(
        id,
        userId,
        startDate,
        endDate
    )
}
