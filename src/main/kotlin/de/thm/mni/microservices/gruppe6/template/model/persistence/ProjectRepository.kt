package de.thm.mni.microservices.gruppe6.template.model.persistence

import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface ProjectRepository: ReactiveCrudRepository<Project, Long>
