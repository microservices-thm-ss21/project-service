package de.thm.mni.microservices.gruppe6.template

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TemplateServiceApplication

fun main(args: Array<String>) {
    runApplication<TemplateServiceApplication>(*args)
}
