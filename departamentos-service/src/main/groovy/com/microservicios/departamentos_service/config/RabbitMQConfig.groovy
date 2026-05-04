package com.microservicios.departamentos_service.config

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
    
    @Bean
    FanoutExchange empleadosEventsExchange() {
        new FanoutExchange(EXCHANGE_NAME)
    }
    
    @Bean
    MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter()
    }
}
