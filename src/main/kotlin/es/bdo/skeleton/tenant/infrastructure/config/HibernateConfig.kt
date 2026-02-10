package es.bdo.skeleton.tenant.infrastructure.config

import es.bdo.skeleton.tenant.infrastructure.TenantConnectionProvider
import es.bdo.skeleton.tenant.infrastructure.TenantIdentifierResolver
import org.hibernate.cfg.AvailableSettings
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HibernateConfig {

    @Bean
    fun hibernatePropertiesCustomizer(
        tenantConnectionProvider: TenantConnectionProvider,
        tenantIdentifierResolver: TenantIdentifierResolver
    ): HibernatePropertiesCustomizer {
        return HibernatePropertiesCustomizer { hibernateProperties ->
            hibernateProperties[AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER] = tenantConnectionProvider
            hibernateProperties[AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER] = tenantIdentifierResolver
        }
    }
}
