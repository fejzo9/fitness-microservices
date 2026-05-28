package com.app.fitness.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String DELETION_EXCHANGE = "user.deletion.exchange";
    public static final String RESPONSE_EXCHANGE = "user.deletion.response.exchange";
    public static final String RESPONSE_QUEUE = "auth.deletion.response.queue";

    @Bean
    public TopicExchange deletionExchange() {
        return new TopicExchange(DELETION_EXCHANGE);
    }

    @Bean
    public TopicExchange responseExchange() {
        return new TopicExchange(RESPONSE_EXCHANGE);
    }

    @Bean
    public Queue responseQueue() {
        return new Queue(RESPONSE_QUEUE);
    }

    @Bean
    public Binding responseBinding(Queue responseQueue, TopicExchange responseExchange) {
        return BindingBuilder.bind(responseQueue).to(responseExchange).with("user.deletion.response");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
