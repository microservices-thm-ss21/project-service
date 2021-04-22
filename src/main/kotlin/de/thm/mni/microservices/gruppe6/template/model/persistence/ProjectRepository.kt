package de.thm.mni.microservices.gruppe6.template.model.persistence

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import java.util.*

interface ProjectRepository: ReactiveCrudRepository<Project, UUID>
