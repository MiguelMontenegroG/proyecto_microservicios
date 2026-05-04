package com.microservicios.auth_service.config

import org.springframework.amqp.core.FanoutExchange
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.DefaultClassMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMQConfig {
    
    // Exchange de empleados para escuchar eventos
    public static final String EMPLEADOS_EXCHANGE_NAME = 'empleados.events'
    
    // Exchange de auth para publicar eventos
    public static final String AUTH_EXCHANGE_NAME = 'auth.events'
    
    @Bean
    FanoutExchange empleadosEventsExchange() {
        new FanoutExchange(EMPLEADOS_EXCHANGE_NAME)
    }
    
    @Bean
    FanoutExchange authEventsExchange() {
        new FanoutExchange(AUTH_EXCHANGE_NAME)
    }
    
    /**
     * MessageConverter global para todos los listeners de RabbitMQ
     * Configurado para convertir TODOS los mensajes a Map ignorando el TypeId header
     */
    @Bean
    Jackson2JsonMessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter()
        DefaultClassMapper classMapper = new DefaultClassMapper()
        // NO confiar en el header __TypeId__ - siempre convertir a Map
        classMapper.setTrustedPackages(['*'] as String[])
        // Establecer HashMap como tipo por defecto para TODOS los mensajes
        classMapper.setDefaultType(java.util.HashMap.class)
        converter.setClassMapper(classMapper)
        return converter
    }
    
    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter jsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory)
        rabbitTemplate.setExchange(authEventsExchange().name)
        rabbitTemplate.setMessageConverter(jsonMessageConverter)
        
        return rabbitTemplate
    }
}
