package de.thm.mni.microservices.gruppe6.template

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ProjectServiceApplication

fun main(args: Array<String>) {
    runApplication<ProjectServiceApplication>(*args)
}
