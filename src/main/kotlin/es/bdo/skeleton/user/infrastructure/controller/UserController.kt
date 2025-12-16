package es.bdo.skeleton.user.infrastructure.controller

import es.bdo.skeleton.user.application.usecase.GetAllUserUseCase
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(
    private val getAllUserUseCase: GetAllUserUseCase
) {

    @GetMapping
    fun index(): List<Map<String, Any>> {
        val users = getAllUserUseCase.handle()
        return users.map { user ->
            mapOf(
                "id" to user.id.toString(),
                "name" to user.name,
                "email" to user.email
            )
        }
    }
}
