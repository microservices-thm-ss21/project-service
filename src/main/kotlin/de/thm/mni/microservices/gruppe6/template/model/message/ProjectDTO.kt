package de.thm.mni.microservices.gruppe6.template.model.message

import java.util.*

/**
 * DTO = Data Transport Object
 */
class ProjectDTO {
    var name: String? = null
    var creator_id: UUID? = null
    var members: Map<UUID, String>? = null //Key=User-Id Value=Role
}
