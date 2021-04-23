package de.thm.mni.microservices.gruppe6.template.model.persistence

import org.springframework.data.relational.core.mapping.Table
import java.util.*


@Table("members")
data class Member(
        var project_id: UUID,
        var user_id: UUID,
        var project_role: String
)