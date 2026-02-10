package es.bdo.skeleton.absence.infrastructure.model

import es.bdo.skeleton.absence.domain.Absence

fun AbsenceEntity.toDomain() = Absence(
    id,
    userId,
    startDate,
    endDate
)

fun Absence.toEntity() = AbsenceEntity(
    id,
    userId,
    startDate,
    endDate
)
