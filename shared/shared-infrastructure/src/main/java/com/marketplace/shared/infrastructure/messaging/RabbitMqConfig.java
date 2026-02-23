package com.marketplace.shared.infrastructure.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String DOMAIN_EXCHANGE = "marketplace.domain.events";

    // Example queue for domain events that can be used later or by different services
    public static final String DOMAIN_EVENTS_QUEUE = "marketplace.queue.domain-events";

    @Bean
    public TopicExchange domainExchange() {
        return new TopicExchange(DOMAIN_EXCHANGE);
    }

    @Bean
    public Queue domainEventsQueue() {
        // durable = true
        return new Queue(DOMAIN_EVENTS_QUEUE, true);
    }

    // Bind all domain events to this queue for general purpose/audit if needed
    @Bean
    public Binding domainEventsBinding(Queue domainEventsQueue, TopicExchange domainExchange) {
        return BindingBuilder.bind(domainEventsQueue).to(domainExchange).with("domain.#");
    }

    /**
     * Essential for routing JSON instead of generic bytes.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
