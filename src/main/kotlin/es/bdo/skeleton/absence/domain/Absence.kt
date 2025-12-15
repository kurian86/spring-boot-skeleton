package es.bdo.skeleton.absence.domain

import java.time.LocalDate
import java.util.*

data class Absence(
    val id: UUID,
    val userId: UUID,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
)
