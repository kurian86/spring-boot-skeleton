package es.bdo.skeleton.absence.domain

interface IAbsenceRepository {
    fun findAll(): List<Absence>
}
