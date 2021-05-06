package de.thm.mni.microservices.gruppe6.project.model.persistence

import de.thm.mni.microservices.gruppe6.project.model.message.ProjectDTO
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.*

@Table("projects")
data class Project(
        @Id var id: UUID?,
        var name: String,
        var creatorId: UUID?,
        var createTime: LocalDateTime,
) {
    constructor(projectDTO: ProjectDTO) : this(
            null,
            projectDTO.name!!,
            projectDTO.creatorId,
            LocalDateTime.now()
    )
}

