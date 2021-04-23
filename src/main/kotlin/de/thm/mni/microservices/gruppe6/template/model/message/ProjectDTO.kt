package de.thm.mni.microservices.gruppe6.template.model.message

import de.thm.mni.microservices.gruppe6.template.model.persistence.Member
import java.util.*

/**
 * DTO = Data Transport Object
 */
class ProjectDTO {
    var name: String? = null
    var creator_id: UUID? = null
    var members: List<Member>? = null
}
