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


@Configuration
@EnableJms
class BrokerConfig {

    @Bean
    fun jmsListenerContainerFactory(activeMQConnectionFactory: ActiveMQConnectionFactory): DefaultJmsListenerContainerFactory {
        val factory = DefaultJmsListenerContainerFactory()
        factory.setPubSubDomain(true)
        factory.setConnectionFactory(activeMQConnectionFactory)
        factory.setMessageConverter(jacksonJmsMessageConverter())
        return factory
    }

    @Bean(name = ["sender"])
    fun jmsTemplate(activeMQConnectionFactory: ActiveMQConnectionFactory): JmsTemplate {
        val jmsTemplate = JmsTemplate()
        jmsTemplate.connectionFactory = activeMQConnectionFactory
        jmsTemplate.isPubSubDomain = true
        jmsTemplate.defaultDestinationName = "microservices.events"
        return jmsTemplate
    }

    fun jacksonJmsMessageConverter(): MessageConverter {
        val converter = MappingJackson2MessageConverter()
        converter.setTargetType(MessageType.TEXT)
        converter.setTypeIdPropertyName("_type")
        return converter
    }

    @Bean
    fun activeMQConnectionFactory(@Value("\${spring.activemq.broker-url}") brokerUrl: String?): ActiveMQConnectionFactory? {
        val activeMQConnectionFactory = ActiveMQConnectionFactory(brokerUrl)
        activeMQConnectionFactory.trustedPackages = listOf("de.thm.mni.microservices", "java.util", "java.time")
        return activeMQConnectionFactory
    }
}
