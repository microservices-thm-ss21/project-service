package de.thm.mni.microservices.gruppe6.project.event

import org.apache.activemq.ActiveMQConnectionFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jms.annotation.EnableJms
import org.springframework.jms.config.DefaultJmsListenerContainerFactory
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.support.converter.MappingJackson2MessageConverter
import org.springframework.jms.support.converter.MessageConverter
import org.springframework.jms.support.converter.MessageType


/**
 * Configuration for both Event sender and receiver
 */
@Configuration
@EnableJms
class BrokerConfig {

    /**
     * Creates the Listener Container Factory to be used within the receiver to create a session and convert the message
     * @param activeMQConnectionFactory Factory to create a connection
     * @return factory used to create a connection context
     */
    @Bean
    fun jmsListenerContainerFactory(activeMQConnectionFactory: ActiveMQConnectionFactory): DefaultJmsListenerContainerFactory {
        val factory = DefaultJmsListenerContainerFactory()
        factory.setPubSubDomain(true)
        factory.setSubscriptionDurable(true)
        factory.setClientId("project-service")
        factory.setConnectionFactory(activeMQConnectionFactory)
        factory.setMessageConverter(jacksonJmsMessageConverter())
        return factory
    }

    /**
     * Used as sender, `convertAndSend(EventTopic.*, Event)` to send data and define a topic, while having a backup topic if undeclared.
     * @param activeMQConnectionFactory Required to establish a connection when sending data
     * @return sender ready to be used with `convertAndSend`
     */
    @Bean(name = ["sender"])
    fun jmsTemplate(activeMQConnectionFactory: ActiveMQConnectionFactory): JmsTemplate {
        val jmsTemplate = JmsTemplate()
        jmsTemplate.connectionFactory = activeMQConnectionFactory
        jmsTemplate.isPubSubDomain = true
        jmsTemplate.defaultDestinationName = "microservices.events"
        return jmsTemplate
    }

    /**
     * Typical message converter to text, using "_type" as TypeId property. To be used in both sender and receiver
     * @return Message converter for text
     */
    fun jacksonJmsMessageConverter(): MessageConverter {
        val converter = MappingJackson2MessageConverter()
        converter.setTargetType(MessageType.TEXT)
        converter.setTypeIdPropertyName("_type")
        return converter
    }

    /**
     * Factory to create an active connection on the given brokerURL with trusted packages predefined.
     * @param brokerUrl ActiveMQ URL to establish connection with
     * @return Specified ActiveMQ-Factory for the microservices workspace
     */
    @Bean
    fun activeMQConnectionFactory(@Value("\${spring.activemq.broker-url}") brokerUrl: String?): ActiveMQConnectionFactory? {
        val activeMQConnectionFactory = ActiveMQConnectionFactory(brokerUrl)
        activeMQConnectionFactory.trustedPackages = listOf("de.thm.mni.microservices", "java.util", "java.time")
        return activeMQConnectionFactory
    }
}
