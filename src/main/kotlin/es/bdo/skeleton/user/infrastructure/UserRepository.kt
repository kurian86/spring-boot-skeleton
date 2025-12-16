package es.bdo.skeleton.user.infrastructure

import es.bdo.skeleton.user.domain.IUserRepository
import es.bdo.skeleton.user.domain.User
import jooq.generated.tables.references.USERS
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

@Repository
class UserRepository(
    private val dsl: DSLContext
) : IUserRepository {

    override fun findAll(): List<User> {
        return dsl.selectFrom(USERS)
            .fetch()
            .map { record ->
                User(
                    id = record.id,
                    name = record.name,
                    email = record.email
                )
            }
    }
}
