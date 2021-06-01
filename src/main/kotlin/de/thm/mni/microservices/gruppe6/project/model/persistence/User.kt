package de.thm.mni.microservices.gruppe6.project.model.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("users")
data class User(
    @Id var id: UUID?
)

