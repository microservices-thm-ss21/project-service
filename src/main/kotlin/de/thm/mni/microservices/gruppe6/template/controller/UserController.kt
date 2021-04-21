package de.thm.mni.microservices.gruppe6.template.controller

import de.thm.mni.microservices.gruppe6.template.model.persistence.User
import de.thm.mni.microservices.gruppe6.template.model.message.UserDTO
import de.thm.mni.microservices.gruppe6.template.service.UserDbService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/users")
class UserController(@Autowired val userService: UserDbService) {

    @GetMapping("/")
    fun getAllUsers(): Flux<User> = userService.getAllUsers()

    @PutMapping("/")
    fun putUser(@RequestBody userDTO: UserDTO): Mono<User> = userService.putUser(userDTO)

    @PostMapping("/{id}")
    fun updateUser(@PathVariable id: Long, @RequestBody userDTO: UserDTO) = userService.updateUser(id, userDTO)

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long) = userService.deleteUser(id)
}
