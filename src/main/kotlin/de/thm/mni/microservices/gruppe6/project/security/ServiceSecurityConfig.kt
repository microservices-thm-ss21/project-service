package de.thm.mni.microservices.gruppe6.project.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain

@EnableWebFluxSecurity
@Configuration
class ServiceSecurityConfig(private val jwtFilter: JwtFilter) {

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf().disable()
            .cors().disable()
            .logout().disable()
            .httpBasic().disable()
            .addFilterAt(jwtFilter.jwtFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
            .authorizeExchange()
            .pathMatchers("/api/projects/*/members/*/exists")
            .permitAll()
            .and()
            .authorizeExchange()
            .pathMatchers("/api/*/admin/**")
            .hasAuthority("ADMIN")
            .and()
            .authorizeExchange()
            .anyExchange()
            .authenticated()
            .and()
            .build()
    }

    @Bean
    fun userDetailsRepository(): ReactiveUserDetailsService {
        return ReactiveUserDetailsService {
            null
        }
    }

    @Bean
    fun getPasswordEncoding(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

}