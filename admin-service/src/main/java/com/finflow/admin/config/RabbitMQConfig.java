package com.finflow.admin.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_ADMIN_ALERTS = "admin-alert-queue";
    public static final String EXCHANGE_LOAN = "loan-exchange";
    
    // We listen for any status updates that admins need to review
    public static final String ROUTING_KEY_LOAN_STATUS = "loan.status.updated";
    public static final String ROUTING_KEY_DOCUMENT_STATUS = "document.status.updated";

    @Bean
    public Queue adminAlertQueue() {
        return new Queue(QUEUE_ADMIN_ALERTS);
    }

    @Bean
    public TopicExchange loanExchange() {
        return new TopicExchange(EXCHANGE_LOAN);
    }

    @Bean
    public Binding bindingLoanAlerts(Queue adminAlertQueue, TopicExchange loanExchange) {
        return BindingBuilder.bind(adminAlertQueue).to(loanExchange).with(ROUTING_KEY_LOAN_STATUS);
    }

    @Bean
    public Binding bindingDocumentAlerts(Queue adminAlertQueue, TopicExchange loanExchange) {
        return BindingBuilder.bind(adminAlertQueue).to(loanExchange).with(ROUTING_KEY_DOCUMENT_STATUS);
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
