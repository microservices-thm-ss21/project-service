package de.thm.mni.microservices.gruppe6.project.security

import de.thm.mni.microservices.gruppe6.lib.classes.authentication.ServiceAuthentication
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.util.function.Predicate

@Component
class JwtFilter(private val jwtService: JwtService) {

    fun jwtFilter(): AuthenticationWebFilter {
        val authManager = jwtAuthenticationManager()
        val jwtFilter = AuthenticationWebFilter(authManager)
        jwtFilter.setServerAuthenticationConverter(JwtAuthenticationConverter())
        return jwtFilter
    }

    fun jwtAuthenticationManager() = ReactiveAuthenticationManager { auth ->
        Mono.create {
            val jwt = auth.credentials as String
            val user = jwtService.authorize(jwt)
            it.success(ServiceAuthentication(user, jwt))
        }
    }

    /**
     * Nested class converting a ServerWebExchange to an Authentication object
     * containing the JWT extracted from the request as credentials.
     */
    class JwtAuthenticationConverter: ServerAuthenticationConverter {
        private val bearer = "Bearer "
        private val matchBearerLength = Predicate { authValue: String -> authValue.length > bearer.length }
        private fun isolateBearerValue(authValue: String) = Mono.just(
            authValue.substring(bearer.length)
        )

        private fun extract(serverWebExchange: ServerWebExchange): Mono<String> {
            return Mono.justOrEmpty(
                serverWebExchange.request
                    .headers
                    .getFirst(HttpHeaders.AUTHORIZATION)
            )
        }

        private fun createAuthenticationObject(jwt: String): Mono<ServiceAuthentication> {
            return Mono.create {
                it.success(ServiceAuthentication(jwt))
            }
        }

        override fun convert(exchange: ServerWebExchange): Mono<Authentication> {
            return exchange.toMono()
                .flatMap(this::extract)
                .filter(matchBearerLength)
                .flatMap(this::isolateBearerValue)
                .flatMap(this::createAuthenticationObject)
        }
    }
}

