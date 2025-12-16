package es.bdo.skeleton.user.infrastructure.controller

import es.bdo.skeleton.shared.extensions.tenantId
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController {

    @GetMapping
    fun index(request: HttpServletRequest): String {
        return "User API is working! Current tenant: ${request.tenantId}"
    }
}
