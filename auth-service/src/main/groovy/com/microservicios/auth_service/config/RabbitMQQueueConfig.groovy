package com.microservicios.auth_service.config

import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.listener.MethodRabbitListenerEndpoint
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar
import org.springframework.amqp.support.converter.SimpleMessageConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory

@Configuration
class RabbitMQQueueConfig implements RabbitListenerConfigurer {
    
    @Value('${spring.application.name:auth-service}')
    String serviceName
    
    @Bean
    Queue authServiceQueue() {
        // Cada instancia tiene su propia cola con nombre único
        new Queue("${serviceName}.queue", true)
    }
    
    @Bean
    Binding authServiceBindingToEmpleados(Queue authServiceQueue, @org.springframework.beans.factory.annotation.Qualifier('empleadosEventsExchange') FanoutExchange empleadosEventsExchange) {
        // Suscribirse SOLO al exchange de empleados para crear usuarios automáticamente
        BindingBuilder.bind(authServiceQueue).to(empleadosEventsExchange)
    }
    
    // NOTA: NO nos suscribimos a auth.events para evitar recibir nuestros propios eventos
    
    /**
     * Container Factory personalizado que usa SimpleMessageConverter
     * Esto permite recibir mensajes como byte[] sin intentar deserializar
     * El listener se encarga de la deserialización manual con ObjectMapper
     */
    @Bean
    SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory()
        factory.setConnectionFactory(connectionFactory)
        // Usar SimpleMessageConverter que pasa los bytes crudos sin intentar convertir
        factory.setMessageConverter(new SimpleMessageConverter())
        return factory
    }
    
    @Override
    void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
        registrar.setMessageHandlerMethodFactory(messageHandlerMethodFactory())
    }
    
    @Bean
    DefaultMessageHandlerMethodFactory messageHandlerMethodFactory() {
        def factory = new DefaultMessageHandlerMethodFactory()
        return factory
    }
}
