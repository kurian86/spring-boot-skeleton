package es.bdo.skeleton.absence.infrastructure

import es.bdo.skeleton.absence.domain.Absence
import es.bdo.skeleton.absence.domain.IAbsenceRepository
import jooq.generated.tables.references.ABSENCES
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

@Repository
class AbsenceRepository(
    private val dsl: DSLContext
) : IAbsenceRepository {

    override fun findAll(): List<Absence> {
        return dsl.selectFrom(ABSENCES)
            .fetch()
            .map { record ->
                Absence(
                    id = record.id,
                    userId = record.userId,
                    startDate = record.startDate,
                    endDate = record.endDate
                )
            }
    }
}
