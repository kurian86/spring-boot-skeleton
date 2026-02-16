package es.bdo.skeleton.absence.domain

interface AbsenceRepository {
    fun count(): Long

    fun findAll(): List<Absence>
}
