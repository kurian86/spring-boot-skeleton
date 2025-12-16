package es.bdo.skeleton

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class SkeletonApplication

fun main(args: Array<String>) {
    runApplication<SkeletonApplication>(*args)
}
