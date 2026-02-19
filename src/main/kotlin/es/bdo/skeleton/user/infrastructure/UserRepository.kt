package es.bdo.skeleton.user.infrastructure

import es.bdo.skeleton.shared.model.FilterGroup
import es.bdo.skeleton.shared.model.Sort
import es.bdo.skeleton.user.domain.User
import es.bdo.skeleton.user.infrastructure.model.toDomain
import es.bdo.skeleton.user.infrastructure.model.toEntity
import es.bdo.skeleton.user.infrastructure.specification.UserFilterSpecification
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import es.bdo.skeleton.user.domain.UserRepository as IUserRepository

@Repository
class UserRepository(
    private val jpaRepository: UserJpaRepository,
    private val filterSpecification: UserFilterSpecification
) : IUserRepository {

    override fun count(filters: List<FilterGroup>): Long {
        val spec = filterSpecification.toSpecification(filters)
        return if (spec != null) {
            jpaRepository.count(spec)
        } else {
            jpaRepository.count()
        }
    }

    override fun findAll(
        offset: Long,
        limit: Int,
        sort: Sort?,
        filters: List<FilterGroup>
    ): List<User> {
        val spec = filterSpecification.toSpecification(filters)
        val pageable = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            buildSpringSort(sort)
        )

        val entities = if (spec != null) {
            jpaRepository.findAll(spec, pageable).content
        } else {
            jpaRepository.findAll(pageable).content
        }

        return entities.map { it.toDomain() }
    }

    private fun buildSpringSort(sort: Sort?): org.springframework.data.domain.Sort {
        if (sort == null) return org.springframework.data.domain.Sort.unsorted()
        
        return org.springframework.data.domain.Sort.by(
            sort.direction,
            sort.property
        )
    }

    override fun findByEmail(email: String): User? {
        return jpaRepository.findByEmail(email)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun save(user: User): User {
        val entity = user.toEntity()
        val savedEntity = jpaRepository.save(entity)
        return savedEntity.toDomain()
    }
}
