package es.bdo.skeleton.tenant.infrastructure.security

import com.nimbusds.jwt.JWTParser
import es.bdo.skeleton.tenant.application.TenantContext
import es.bdo.skeleton.tenant.application.exception.TenantNotFoundException
import es.bdo.skeleton.tenant.domain.TenantRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class TenantContextFilter(
    private val repository: TenantRepository,
) : OncePerRequestFilter() {

    companion object {
        private const val TENANT_HEADER = "X-Tenant-ID"

        private fun extractTenantIdFromJwt(token: String): String? {
            return try {
                val claims = JWTParser.parse(token).jwtClaimsSet
                claims.getStringClaim("tenant_id")
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")
        val headerTenantId = request.getHeader(TENANT_HEADER) ?: TenantContext.DEFAULT_TENANT

        // Extract tenant_id from JWT if present
        val jwtTenantId = authHeader?.let { header ->
            if (header.startsWith("Bearer ")) {
                extractTenantIdFromJwt(header.substring(7))
            } else null
        }

        // Validate tenant_id matches
        if (jwtTenantId != null && jwtTenantId != headerTenantId) {
            response.status = HttpStatus.FORBIDDEN.value()
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.writer.write("""{"error": "Tenant mismatch", "message": "JWT tenant_id does not match X-Tenant-ID header"}""")
            return
        }

        val tenantId = jwtTenantId ?: headerTenantId

        if (tenantId.isBlank()) {
            throw TenantNotFoundException("Tenant ID cannot be blank")
        }

        val tenant = repository.findById(tenantId)
            ?: throw TenantNotFoundException("Tenant not found: $tenantId")

        if (!tenant.isActive) {
            repository.evictCache(tenantId)
            throw TenantNotFoundException("Tenant is not active: $tenantId")
        }

        TenantContext.withTenant(tenantId) {
            filterChain.doFilter(request, response)
        }
    }
}
