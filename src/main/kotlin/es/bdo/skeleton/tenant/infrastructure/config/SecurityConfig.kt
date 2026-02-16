package es.bdo.skeleton.tenant.infrastructure.config

import es.bdo.skeleton.tenant.domain.OAuthProviderRepository
import es.bdo.skeleton.tenant.infrastructure.security.TenantAuthenticationManagerResolver
import es.bdo.skeleton.tenant.infrastructure.security.TenantContextFilter
import es.bdo.skeleton.tenant.infrastructure.security.jwt.TenantJwtAuthenticationConverter
import es.bdo.skeleton.tenant.infrastructure.security.jwt.TenantJwtDecoder
import es.bdo.skeleton.tenant.infrastructure.security.jwt.UserInfoExtractorResolver
import es.bdo.skeleton.tenant.infrastructure.security.opaque.OpaqueTokenIntrospectorResolver
import es.bdo.skeleton.tenant.infrastructure.security.opaque.TenantOpaqueTokenIntrospector
import es.bdo.skeleton.user.application.UserProvider
import jakarta.servlet.http.HttpServletRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManagerResolver
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        authenticationManagerResolver: AuthenticationManagerResolver<HttpServletRequest>,
        tenantContextFilter: TenantContextFilter
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
                oauth2.authenticationManagerResolver(authenticationManagerResolver)
            }

        return http.build()
    }

    @Bean
    fun authenticationManagerResolver(
        oauthProviderRepository: OAuthProviderRepository,
        userInfoExtractorResolver: UserInfoExtractorResolver,
        opaqueTokenIntrospectorResolver: OpaqueTokenIntrospectorResolver,
        userProvider: UserProvider
    ): AuthenticationManagerResolver<HttpServletRequest> {
        return TenantAuthenticationManagerResolver(
            TenantJwtDecoder(oauthProviderRepository),
            TenantJwtAuthenticationConverter(userInfoExtractorResolver, userProvider),
            TenantOpaqueTokenIntrospector(opaqueTokenIntrospectorResolver, oauthProviderRepository, userProvider)
        )
    }
}
