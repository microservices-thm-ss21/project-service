package de.thm.mni.microservices.gruppe6.project.model.persistence

import de.thm.mni.microservices.gruppe6.lib.classes.projectService.Project
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import java.util.*

interface ProjectRepository : ReactiveCrudRepository<Project, UUID>
