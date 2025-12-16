package es.bdo.skeleton.tenant.infrastructure.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
@EnableCaching
class CacheConfig(
    private val tenantProperties: TenantProperties
) {

    @Bean
    fun cacheManager(): CacheManager {
        val cacheManager = CaffeineCacheManager("tenants")
        cacheManager.setCaffeine(caffeineCacheBuilder())
        return cacheManager
    }

    private fun caffeineCacheBuilder(): Caffeine<Any, Any> {
        return Caffeine.newBuilder()
            .expireAfterWrite(tenantProperties.cache.ttlSeconds, TimeUnit.SECONDS)
            .maximumSize(1000)
            .recordStats()
    }
}
