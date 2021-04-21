package de.thm.mni.microservices.gruppe6.template.model.message

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

/**
 * DTO = Data Transport Object
 */
class UserDTO {
    var username: String? = null
    var name: String? = null
    var lastName: String? = null
    @JsonFormat(pattern = "dd.MM.yyyy")
    var dateOfBirth: LocalDate? = null
    var globalRole: String? = null
}
