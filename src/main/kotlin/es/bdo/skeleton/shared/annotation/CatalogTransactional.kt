package es.bdo.skeleton.shared.annotation

import org.springframework.core.annotation.AliasFor
import org.springframework.transaction.annotation.Transactional

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Transactional("catalogTransactionManager")
annotation class CatalogTransactional(
    @get:AliasFor(annotation = Transactional::class, attribute = "readOnly")
    val readOnly: Boolean = false
)
