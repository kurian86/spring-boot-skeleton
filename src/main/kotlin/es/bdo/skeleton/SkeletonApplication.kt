package es.bdo.skeleton

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableCaching
class SkeletonApplication

fun main(args: Array<String>) {
    runApplication<SkeletonApplication>(*args)
}
