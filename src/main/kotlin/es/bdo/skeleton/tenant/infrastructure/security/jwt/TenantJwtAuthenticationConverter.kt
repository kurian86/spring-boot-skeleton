package es.bdo.skeleton.tenant.infrastructure.security.jwt

import es.bdo.skeleton.tenant.application.security.UserInfo
import es.bdo.skeleton.user.domain.User
import es.bdo.skeleton.user.domain.UserRepository
import es.bdo.skeleton.user.domain.UserStatus
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import java.time.ZonedDateTime
import java.util.UUID

class TenantJwtAuthenticationConverter(
    private val userRepository: UserRepository,
    private val grantedAuthoritiesConverter: JwtGrantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter(),
) : Converter<Jwt, AbstractAuthenticationToken> {

    override fun convert(jwt: Jwt): AbstractAuthenticationToken {
        val userInfo = extractUserInfo(jwt)

        // Auto-register user if doesn't exist
        ensureUserExists(userInfo)

        val authorities = extractAuthorities(jwt)
        return TenantJwtAuthenticationToken(jwt, authorities, userInfo)
    }

    private fun ensureUserExists(userInfo: UserInfo): User {
        return userRepository.findByEmail(userInfo.email) ?: run {
            val newUser = User(
                id = UUID.randomUUID(),
                name = userInfo.attributes["name"] as? String
                    ?: userInfo.username,
                email = userInfo.email,
                status = UserStatus.ACTIVE,
                externalId = userInfo.subject,
                createdAt = ZonedDateTime.now(),
                updatedAt = ZonedDateTime.now()
            )
            userRepository.save(newUser)
        }
    }

    private fun extractUserInfo(jwt: Jwt): UserInfo {
        return UserInfo.fromAttributes(jwt.claims)
    }

    private fun extractAuthorities(jwt: Jwt): Collection<GrantedAuthority> {
        val authorities = mutableSetOf<GrantedAuthority>()

        // Add standard JWT authorities
        authorities.addAll(grantedAuthoritiesConverter.convert(jwt) ?: emptySet())

        // Add roles from JWT claim
        val roles = jwt.getClaimAsStringList("roles")
        roles?.forEach { role ->
            val authority = if (role.startsWith("ROLE_")) role else "ROLE_$role"
            authorities.add(SimpleGrantedAuthority(authority))
        }

        return authorities
    }
}
