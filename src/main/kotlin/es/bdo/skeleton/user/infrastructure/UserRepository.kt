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
import org.springframework.data.domain.Sort as SpringSort

@Repository
class UserRepository(
    private val jpaRepository: UserJpaRepository,
    private val filterSpecification: UserFilterSpecification
) : IUserRepository {

    override fun findAll(
        offset: Long,
        limit: Int,
        sort: Sort?,
        filters: List<FilterGroup>
    ):  Pair<Long, List<User>> {
        val spec = filterSpecification.toSpecification(filters)
        val pageable = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            buildSpringSort(sort)
        )

        val page = if (spec != null) {
            jpaRepository.findAll(spec, pageable)
        } else {
            jpaRepository.findAll(pageable)
        }

        return page.totalElements to page.content.map { it.toDomain() }
    }

    private fun buildSpringSort(sort: Sort?): SpringSort {
        if (sort == null) return SpringSort.unsorted()
        
        return SpringSort.by(
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
