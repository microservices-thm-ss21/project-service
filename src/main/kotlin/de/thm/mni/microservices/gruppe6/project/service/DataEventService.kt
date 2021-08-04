package de.thm.mni.microservices.gruppe6.project.service

import de.thm.mni.microservices.gruppe6.lib.event.DataEvent
import de.thm.mni.microservices.gruppe6.lib.event.IssueDataEvent
import de.thm.mni.microservices.gruppe6.lib.event.ProjectDataEvent
import de.thm.mni.microservices.gruppe6.lib.event.UserDataEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 * Implements a service which handles all dataEvents incoming
 */
@Service
class DataEventService(
    @Autowired val userDbService: UserDbService
) {

    /**
     * Processes all the dataEvents
     * @param dataEvent
     */
    @Throws(IllegalStateException::class)
    fun processDataEvent(dataEvent: Mono<DataEvent>) {
        dataEvent.subscribe {
            when (it) {
                is UserDataEvent -> {
                    userDbService.receiveUpdate(it)
                }
                is ProjectDataEvent -> {/* Do nothing with own events */
                }
                is IssueDataEvent -> {/* ignore */
                }
                else -> error("Unexpected Event type: ${it?.javaClass}")
            }
        }
    }
}
