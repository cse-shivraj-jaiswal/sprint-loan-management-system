package com.finflow.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_NOTIFICATION = "notification-queue";
    public static final String EXCHANGE_LOAN = "loan-exchange";
    
    public static final String ROUTING_KEY_AUTH = "auth.user.#";
    public static final String ROUTING_KEY_LOAN = "loan.status.#";
    public static final String ROUTING_KEY_DOCUMENT = "document.status.#";

    @Bean
    public Queue notificationQueue() {
        return new Queue(QUEUE_NOTIFICATION);
    }

    @Bean
    public TopicExchange loanExchange() {
        return new TopicExchange(EXCHANGE_LOAN);
    }

    @Bean
    public Binding bindingAuth(Queue notificationQueue, TopicExchange loanExchange) {
        return BindingBuilder.bind(notificationQueue).to(loanExchange).with(ROUTING_KEY_AUTH);
    }

    @Bean
    public Binding bindingLoan(Queue notificationQueue, TopicExchange loanExchange) {
        return BindingBuilder.bind(notificationQueue).to(loanExchange).with(ROUTING_KEY_LOAN);
    }

    @Bean
    public Binding bindingDocument(Queue notificationQueue, TopicExchange loanExchange) {
        return BindingBuilder.bind(notificationQueue).to(loanExchange).with(ROUTING_KEY_DOCUMENT);
    }

    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }
}
