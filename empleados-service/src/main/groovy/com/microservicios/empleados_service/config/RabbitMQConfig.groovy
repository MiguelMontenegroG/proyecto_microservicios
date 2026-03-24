package com.microservicios.empleados_service.config

import org.springframework.amqp.core.FanoutExchange
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
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
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory)
       rabbitTemplate.setExchange(empleadosEventsExchange().name)
       rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter())
        return rabbitTemplate
    }
}
