package de.thm.mni.microservices.gruppe6.project.model.message

import java.util.*

/**
 * DTO = Data Transport Object
 */
class ProjectDTO {
    var name: String? = null
    var creatorId: UUID? = null
    var members: List<MemberDTO>? = null
}
