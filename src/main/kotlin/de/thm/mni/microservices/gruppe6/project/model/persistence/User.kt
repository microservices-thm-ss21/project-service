package de.thm.mni.microservices.gruppe6.project.model.persistence

import de.thm.mni.microservices.gruppe6.project.model.message.ProjectDTO
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.*

@Table("users")
data class User(
        @Id var id: UUID?
)

