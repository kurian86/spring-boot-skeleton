package es.bdo.skeleton.tenant.domain

interface ITenantRepository {
    fun findAllActive(): List<Tenant>

    fun findById(id: String): Tenant?
}
