package es.bdo.skeleton.absence.infrastructure.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import java.util.*

@Entity
@Table(name = "absences")
data class AbsenceEntity(
    @Id
    @Column(name = "id", nullable = false)
    val id: UUID,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "start_date", nullable = false)
    val startDate: LocalDate,

    @Column(name = "end_date", nullable = true)
    val endDate: LocalDate? = null
)
