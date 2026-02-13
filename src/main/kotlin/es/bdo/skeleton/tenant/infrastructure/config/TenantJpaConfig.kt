package es.bdo.skeleton.tenant.infrastructure.config

import es.bdo.skeleton.tenant.infrastructure.TenantConnectionProvider
import es.bdo.skeleton.tenant.infrastructure.TenantIdentifierResolver
import jakarta.persistence.EntityManagerFactory
import org.hibernate.cfg.AvailableSettings
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.jpa.EntityManagerFactoryBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = ["es.bdo.skeleton.user", "es.bdo.skeleton.absence"],
    entityManagerFactoryRef = "tenantEntityManagerFactory",
    transactionManagerRef = "tenantTransactionManager"
)
class TenantJpaConfig {

    @Bean
    fun tenantEntityManagerFactory(
        builder: EntityManagerFactoryBuilder,
        dataSource: DataSource,
        tenantConnectionProvider: TenantConnectionProvider,
        tenantIdentifierResolver: TenantIdentifierResolver
    ): LocalContainerEntityManagerFactoryBean {
        return builder
            .dataSource(dataSource)
            .packages("es.bdo.skeleton.user.infrastructure", "es.bdo.skeleton.absence.infrastructure")
            .persistenceUnit("tenant")
            .properties(
                mapOf(
                    AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER to tenantConnectionProvider,
                    AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER to tenantIdentifierResolver
                )
            )
            .build()
    }

    @Bean
    fun tenantTransactionManager(
        @Qualifier("tenantEntityManagerFactory") emf: EntityManagerFactory
    ): PlatformTransactionManager {
        return JpaTransactionManager(emf)
    }
}
