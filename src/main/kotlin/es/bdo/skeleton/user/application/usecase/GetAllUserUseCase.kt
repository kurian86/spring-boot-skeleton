package es.bdo.skeleton.user.application.usecase

import es.bdo.skeleton.user.domain.IUserRepository
import es.bdo.skeleton.user.domain.User
import org.springframework.stereotype.Service

@Service
class GetAllUserUseCase(
    private val repository: IUserRepository
) {
    fun handle(): List<User> {
        return repository.findAll()
    }
}
