package es.bdo.skeleton.user.application.usecase

import es.bdo.skeleton.user.application.model.UserDTO
import es.bdo.skeleton.user.application.model.toDTO
import es.bdo.skeleton.user.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetAllUserUseCase(
    private val repository: UserRepository,
) {

    @Transactional(readOnly = true)
    fun handle(): List<UserDTO> {
        return repository.findAll()
            .map { it.toDTO() }
    }
}
