package de.thm.mni.microservices.gruppe6.template.model.persistence

import de.thm.mni.microservices.gruppe6.template.model.message.ProjectDTO
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("projects")
data class Project(
    @Id var id: Long? = null,
    var name: String,
    var createTime: LocalDateTime,
) {
    constructor(id: Long, projectDTO: ProjectDTO): this(
         id
        ,projectDTO.name!!
        ,LocalDateTime.now()
    )
}

