package es.bdo.skeleton.absence.domain

interface AbsenceRepository {
    fun findAll(): List<Absence>
}
