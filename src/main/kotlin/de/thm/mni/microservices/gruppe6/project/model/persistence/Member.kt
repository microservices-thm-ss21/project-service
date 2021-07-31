package de.thm.mni.microservices.gruppe6.project.model.persistence

import de.thm.mni.microservices.gruppe6.lib.classes.projectService.ProjectRole
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("members")
data class Member(
        @Id var id: UUID?,
        var projectId: UUID,
        var userId: UUID,
        var projectRole: String
) {
    constructor(projectId: UUID, userId: UUID, projectRole: ProjectRole) : this(
            null,
            projectId,
            userId,
            projectRole.name
    )
}
