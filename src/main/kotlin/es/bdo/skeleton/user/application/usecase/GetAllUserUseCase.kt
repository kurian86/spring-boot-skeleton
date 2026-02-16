package es.bdo.skeleton.user.application.usecase

import es.bdo.skeleton.shared.annotation.TenantTransactional
import es.bdo.skeleton.user.application.model.UserDTO
import es.bdo.skeleton.user.application.model.toDTO
import es.bdo.skeleton.user.domain.UserRepository
import org.springframework.stereotype.Service

@Service
class GetAllUserUseCase(
    private val repository: UserRepository,
) {

    @TenantTransactional(readOnly = true)
    fun handle(): List<UserDTO> {
        return repository.findAll()
            .map { it.toDTO() }
    }
}
