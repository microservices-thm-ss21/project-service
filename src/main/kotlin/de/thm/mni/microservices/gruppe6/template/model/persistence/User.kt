package de.thm.mni.microservices.gruppe6.template.model.persistence

import de.thm.mni.microservices.gruppe6.template.model.message.UserDTO
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import java.time.LocalDateTime

@Table("users")
data class User(
    @Id var id: Long? = null,
    var username: String,
    var name: String,
    var lastName: String,
    var dateOfBirth: LocalDate,
    var createTime: LocalDateTime,
    var globalRole: String,
    var lastLogin: LocalDateTime?
) {
    constructor(id: Long, userDTO: UserDTO): this(
         id
        ,userDTO.username!!
        ,userDTO.name!!
        ,userDTO.lastName!!
        ,userDTO.dateOfBirth!!
        ,LocalDateTime.now()
        ,userDTO.globalRole!!
        ,null
    )

}

