package de.thm.mni.microservices.gruppe6.project.requests

import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets

@Component
class Requester {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun getResponseSpec(baseURI: String, routeURI: String): WebClient.RequestHeadersSpec<*> {
        val client = WebClient.create(baseURI)
        val uriSpec: WebClient.RequestHeadersUriSpec<*> = client.get()
        val headerSpec: WebClient.RequestHeadersSpec<*> = uriSpec.uri(routeURI) //uriSpec.uri("api/projects/user/$userId")
        return headerSpec.header(
            HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE
        )
            .accept(MediaType.APPLICATION_JSON)
            .acceptCharset(StandardCharsets.UTF_8)
    }

    fun <T> forwardGetRequestFlux(baseURI: String, routeURI: String, returnClass: Class<T>): Flux<T> {
        return getResponseSpec(baseURI, routeURI).exchangeToFlux { response: ClientResponse ->
            logger.debug("$response")
            logger.debug("${response.statusCode()}")
            if (response.statusCode() == HttpStatus.OK) {
                logger.debug("Everything ok")
                response.bodyToFlux(returnClass)
            } else {
                logger.debug("Result Flux Empty")
                Flux.empty()
            }
        }
    }

    fun <T> forwardGetRequestMono(baseURI: String, routeURI: String, returnClass: Class<T>): Mono<T> {
        return getResponseSpec(baseURI, routeURI).exchangeToMono { response: ClientResponse ->
            logger.debug(response.toString())
            logger.debug("${response.statusCode()}")
            if (response.statusCode() == HttpStatus.OK) {
                logger.debug("Everything ok")
                response.bodyToMono(returnClass)
            } else {
                logger.debug("Result Mono Empty")
                Mono.empty()
            }
        }
    }
}