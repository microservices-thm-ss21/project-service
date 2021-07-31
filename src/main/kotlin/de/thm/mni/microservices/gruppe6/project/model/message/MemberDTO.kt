package de.thm.mni.microservices.gruppe6.project.model.message

import java.util.*

/**
 * DTO = Data Transport Object
 */
data class MemberDTO (
    val userId: UUID,
    val projectRole: String
)
