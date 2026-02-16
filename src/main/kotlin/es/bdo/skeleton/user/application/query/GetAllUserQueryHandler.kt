package es.bdo.skeleton.user.application.query

import es.bdo.skeleton.shared.annotation.TenantTransactional
import es.bdo.skeleton.shared.cqrs.QueryHandler
import es.bdo.skeleton.shared.model.PaginationResult
import es.bdo.skeleton.user.application.model.UserDTO
import es.bdo.skeleton.user.application.model.toDTO
import es.bdo.skeleton.user.domain.UserRepository
import org.springframework.stereotype.Service

@Service
class GetAllUserQueryHandler(
    private val repository: UserRepository
) : QueryHandler<GetAllUserQuery, PaginationResult<UserDTO>> {

    @TenantTransactional(readOnly = true)
    override fun handle(query: GetAllUserQuery): Result<PaginationResult<UserDTO>> {
        return runCatching {
            val total = repository.count()
            val items = repository.findAll()
                .map { it.toDTO() }

            PaginationResult.from(total, items)
        }
    }
}
