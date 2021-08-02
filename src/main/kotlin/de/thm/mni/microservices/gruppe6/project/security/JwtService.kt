package de.thm.mni.microservices.gruppe6.project.security

import de.thm.mni.microservices.gruppe6.lib.classes.userService.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.Key

@Component
class JwtService(@Value(value = "\${jwt.secret}") private val secret: String) {

    private val key: Key = Keys.hmacShaKeyFor(secret.toByteArray())
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun authorize(jwt: String): User? {
        return try {
            val claims: Claims =
                Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(jwt)
                    .body
            User(claims)
        } catch (e: Exception) {
            logger.error("Jwt handling exception!", e)
            null
        }
    }

}
