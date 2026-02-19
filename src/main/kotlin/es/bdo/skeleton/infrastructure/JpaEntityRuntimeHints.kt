package es.bdo.skeleton.infrastructure

import es.bdo.skeleton.absence.infrastructure.model.AbsenceEntity
import es.bdo.skeleton.tenant.infrastructure.model.ConfigEntity
import es.bdo.skeleton.tenant.infrastructure.model.TenantEntity
import es.bdo.skeleton.user.infrastructure.model.UserEntity
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.ImportRuntimeHints

@Configuration
@ImportRuntimeHints(JpaEntityRuntimeHints::class)
class JpaEntityRuntimeHintsRegistrar

class JpaEntityRuntimeHints : RuntimeHintsRegistrar {
    override fun registerHints(hints: RuntimeHints, classLoader: ClassLoader?) {
        hints.reflection().registerType(TenantEntity::class.java)
        hints.reflection().registerType(ConfigEntity::class.java)
        hints.reflection().registerType(UserEntity::class.java)
        hints.reflection().registerType(AbsenceEntity::class.java)
    }
}
