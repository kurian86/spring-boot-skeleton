package es.bdo.skeleton.user.application.usecase

import es.bdo.skeleton.user.application.UserProvider
import es.bdo.skeleton.user.domain.User
import org.springframework.stereotype.Service

@Service
class GetAllUserUseCase(
    private val provider: UserProvider,
) {
    fun handle(): List<User> {
        return provider.findAll()
    }
}
