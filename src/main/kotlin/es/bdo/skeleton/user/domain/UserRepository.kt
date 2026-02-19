package es.bdo.skeleton.user.domain

import es.bdo.skeleton.shared.model.FilterGroup
import es.bdo.skeleton.shared.model.Sort

interface UserRepository {
    fun findAll(offset: Long, limit: Int, sort: Sort?, filters: List<FilterGroup>): Pair<Long, List<User>>

    fun findByEmail(email: String): User?

    fun save(user: User): User
}
