package es.bdo.skeleton.absence.infrastructure

import es.bdo.skeleton.absence.infrastructure.model.AbsenceEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.util.*

interface AbsenceJpaRepository : JpaRepository<AbsenceEntity, UUID>, JpaSpecificationExecutor<AbsenceEntity>
