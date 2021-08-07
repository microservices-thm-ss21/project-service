package de.thm.mni.microservices.gruppe6.project.event


import de.thm.mni.microservices.gruppe6.lib.event.DataEvent
import de.thm.mni.microservices.gruppe6.lib.event.DeletedIssuesSagaEvent
import de.thm.mni.microservices.gruppe6.lib.event.DomainEvent
import de.thm.mni.microservices.gruppe6.lib.event.EventTopic
import de.thm.mni.microservices.gruppe6.project.saga.service.ProjectDeletedSagaService
import de.thm.mni.microservices.gruppe6.project.service.DataEventService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jms.annotation.JmsListener
import org.springframework.jms.annotation.JmsListeners
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import javax.jms.Message
import javax.jms.ObjectMessage

/**
 * Class used to receive any Events regarding the microservices workspace
 * @param dataEventService A service to transfer the events into the project specific context
 */
@Component
class Receiver(
    private val dataEventService: DataEventService,
    private val projectDeletedSagaService: ProjectDeletedSagaService) {

    /**
     * Logger to track errors within receiving message and debugging not implemented message types
     */
    val logger: Logger = LoggerFactory.getLogger(this::class.java)


    /**
     * Listen to all topics specified via the JMSListener destinations, distributes the messages to the corresponding services
     * The ContainerFactory is required to specify the receiving context.
     * @param message a Object message, containing either a DataEvent or DomainEvent within its `object`-payload.
     */
    @JmsListeners(
        JmsListener(destination = EventTopic.DataEvents.topic, containerFactory = "jmsListenerContainerFactory")
    )
    fun receive(message: Message) {
        try {
            if (message !is ObjectMessage) {
                logger.error("Received unknown message type {} with id {}", message.jmsType, message.jmsMessageID)
                return
            }
            when (val payload = message.`object`) {
                is DataEvent -> {
                    logger.debug("Received DataEvent ObjectMessage with code {} and id {}", payload.code, payload.id)
                    dataEventService.processDataEvent(Mono.just(payload))
                }
                is DomainEvent -> {
                    logger.debug("Received DomainEvent Object Message with code {}", payload.code)
                    /** Do nothing for now / forever with domain events
                     * No use within project service */
                    logger.error(
                        "Received DomainEvent within ProjectService with code {}",
                        payload.code
                    )
                }
                is DeletedIssuesSagaEvent -> {
                    logger.debug("Received DeletedIssuesSagaEvent reference {}/{} and id {}", payload.referenceType, payload.referenceValue, payload.success)
                    projectDeletedSagaService.receiveSagaEvent(payload)
                }
                else -> {
                    logger.error(
                        "Received unknown ObjectMessage with payload type {} with id {}",
                        payload.javaClass,
                        message.jmsMessageID
                    )
                }
            }
        } catch (e: Exception) {
            logger.error("Receiver-Error", e)
        }
    }

    /**
     * Listen to all topics specified via the JMSListener destinations, distributes the messages to the corresponding services
     * Special listener for SagaEvents.
     * @param message an Object message, containing a SagaEvent within its `object`-payload.
     */
    @JmsListeners(
        JmsListener(destination = EventTopic.SagaEvents.topic, containerFactory = "jmsSagaEventListenerContainerFactory")
    )
    fun receiveSaga(message: Message) {
        try {
            if (message !is ObjectMessage) {
                logger.error("Received unknown message type {} with id {}", message.jmsType, message.jmsMessageID)
                return
            }
            when (val payload = message.`object`) {
                is DeletedIssuesSagaEvent -> {
                    logger.debug(
                        "Received DeletedIssuesSagaEvent reference {}/{} and id {}",
                        payload.referenceType,
                        payload.referenceValue,
                        payload.success
                    )
                    projectDeletedSagaService.receiveSagaEvent(payload)
                }
            }
            when (val payload = message.`object`) {
                else -> {
                    logger.error(
                        "Received unknown ObjectMessage with payload type {} with id {}",
                        payload.javaClass,
                        message.jmsMessageID
                    )
                }
            }
        } catch (e: Exception) {
            logger.error("Receiver-Error", e)
        }
    }
}
