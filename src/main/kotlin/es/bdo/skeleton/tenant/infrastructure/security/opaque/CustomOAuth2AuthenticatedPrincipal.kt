package es.bdo.skeleton.tenant.infrastructure.security.opaque

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal

class CustomOAuth2AuthenticatedPrincipal(
    private val principalName: String,
    private val principalAttributes: Map<String, Any>,
    private val principalAuthorities: Collection<GrantedAuthority>
) : OAuth2AuthenticatedPrincipal {
    
    override fun getName(): String = principalName
    
    override fun getAttributes(): Map<String, Any> = principalAttributes
    
    override fun getAuthorities(): Collection<GrantedAuthority> = principalAuthorities
    
    @Suppress("UNCHECKED_CAST")
    override fun <A : Any> getAttribute(name: String): A? {
        return principalAttributes[name] as? A
    }
}
