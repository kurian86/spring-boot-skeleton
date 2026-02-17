package es.bdo.skeleton.tenant.infrastructure.config

import es.bdo.skeleton.tenant.infrastructure.security.TenantContextFilter
import es.bdo.skeleton.tenant.infrastructure.security.jwt.TenantJwtAuthenticationConverter
import es.bdo.skeleton.user.application.UserRegistrationService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig {

    @Bean
    fun jwtAuthenticationConverter(userRegistrationService: UserRegistrationService): Converter<Jwt, AbstractAuthenticationToken> {
        return TenantJwtAuthenticationConverter(userRegistrationService)
    }

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        tenantContextFilter: TenantContextFilter,
        jwtAuthenticationConverter: Converter<Jwt, out AbstractAuthenticationToken>
    ): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .addFilterBefore(tenantContextFilter, BearerTokenAuthenticationFilter::class.java)
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)
                }
            }

        return http.build()
    }
}
