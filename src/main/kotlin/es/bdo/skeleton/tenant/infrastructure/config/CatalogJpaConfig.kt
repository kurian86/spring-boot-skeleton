package es.bdo.skeleton.tenant.infrastructure.config

import es.bdo.skeleton.tenant.infrastructure.model.ConfigEntity
import es.bdo.skeleton.tenant.infrastructure.model.TenantEntity
import jakarta.persistence.EntityManagerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.jpa.EntityManagerFactoryBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypes
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = ["es.bdo.skeleton.tenant"],
    entityManagerFactoryRef = "catalogEntityManagerFactory",
    transactionManagerRef = "catalogTransactionManager"
)
class CatalogJpaConfig {

    @Bean
    @Primary
    fun catalogManagedTypes(): PersistenceManagedTypes =
        PersistenceManagedTypes.of(TenantEntity::class.java.name, ConfigEntity::class.java.name)

    @Bean
    @Primary
    fun catalogEntityManagerFactory(
        builder: EntityManagerFactoryBuilder,
        dataSource: DataSource,
        @Qualifier("catalogManagedTypes") managedTypes: PersistenceManagedTypes
    ): LocalContainerEntityManagerFactoryBean {
        return builder
            .dataSource(dataSource)
            .managedTypes(managedTypes)
            .persistenceUnit("catalog")
            .build()
    }

    @Bean
    @Primary
    fun catalogTransactionManager(
        @Qualifier("catalogEntityManagerFactory") emf: EntityManagerFactory
    ): PlatformTransactionManager {
        return JpaTransactionManager(emf)
    }
}

