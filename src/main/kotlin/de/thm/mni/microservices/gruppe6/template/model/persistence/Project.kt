package de.thm.mni.microservices.gruppe6.template.model.persistence

import de.thm.mni.microservices.gruppe6.template.model.message.ProjectDTO
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.*

@Table("projects")
data class Project(
    @Id var id: UUID?,
    var name: String,
    var createTime: LocalDateTime,
) {
    constructor(id: UUID?, projectDTO: ProjectDTO): this(
        id,
        projectDTO.name!!
        ,LocalDateTime.now()
    )
}

