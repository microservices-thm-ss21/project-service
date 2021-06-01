package de.thm.mni.microservices.gruppe6.project.model.persistence

import de.thm.mni.microservices.gruppe6.project.model.message.MemberDTO
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
    constructor(projectId: UUID, memberDTO: MemberDTO) : this(
            null,
            projectId,
            memberDTO.userId!!,
            memberDTO.projectRole!!
    )
}
