package de.thm.mni.microservices.gruppe6.template.model.message

import de.thm.mni.microservices.gruppe6.template.model.persistence.Member
import java.util.*

/**
 * DTO = Data Transport Object
 */
class MemberDTO {
    var userId: UUID? = null
    var projectRole: String? = null
}
