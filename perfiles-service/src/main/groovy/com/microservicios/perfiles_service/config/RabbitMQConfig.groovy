package com.microservicios.perfiles_service.config

import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMQConfig {
    
    public static final String EXCHANGE_NAME = 'empleados.events'
    public static final String COLA_PERFILES = 'cola.perfiles'
    
    @Bean
    FanoutExchange empleadosEventsExchange() {
        new FanoutExchange(EXCHANGE_NAME)
    }
    
    @Bean
    Queue colaPerfiles() {
        return QueueBuilder.durable(COLA_PERFILES).build()
    }
    
    @Bean
    Binding bindingColaPerfiles(Queue colaPerfiles, FanoutExchange empleadosEventsExchange) {
        BindingBuilder.bind(colaPerfiles).to(empleadosEventsExchange)
    }
    
    @Bean
    SimpleMessageListenerContainer listenerContainer(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer()
       container.setConnectionFactory(connectionFactory)
       container.setQueueNames(COLA_PERFILES)
       container.setConcurrentConsumers(3)
       container.setMaxConcurrentConsumers(10)
       container.setPrefetchCount(10)
        return container
    }
    
    @Bean
    MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter()
    }
}
