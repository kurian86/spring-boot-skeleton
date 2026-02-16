package es.bdo.skeleton.user.application.command

data class RegisterUserCommand(
    val username: String,
    val email: String
)
