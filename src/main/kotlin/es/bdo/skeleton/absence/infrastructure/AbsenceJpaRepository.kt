package es.bdo.skeleton.absence.infrastructure

import es.bdo.skeleton.absence.infrastructure.model.AbsenceEntity
import org.springframework.data.repository.CrudRepository
import java.util.*

interface AbsenceJpaRepository : CrudRepository<AbsenceEntity, UUID>
