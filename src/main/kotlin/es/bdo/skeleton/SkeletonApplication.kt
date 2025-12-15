package es.bdo.skeleton

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SkeletonApplication

fun main(args: Array<String>) {
	runApplication<SkeletonApplication>(*args)
}
