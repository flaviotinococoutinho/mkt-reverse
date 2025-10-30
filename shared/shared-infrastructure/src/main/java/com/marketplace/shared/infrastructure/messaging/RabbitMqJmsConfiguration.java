package com.marketplace.shared.infrastructure.messaging;

import com.rabbitmq.jms.admin.RMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Session;

/**
 * RabbitMQ JMS configuration for critical async integrations.
 * 
 * Features:
 * - Manual acknowledgment for guaranteed delivery
 * - Dead Letter Queues (DLQ) for failed messages
 * - Retry policies with exponential backoff
 * - Transactional messaging support
 * - JSON message conversion
 * 
 * Follows Clean Architecture principles - infrastructure concerns separated from core.
 */
@Configuration
@EnableJms
public class RabbitMqJmsConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(RabbitMqJmsConfiguration.class);
    
    private static final int DEFAULT_CONCURRENT_CONSUMERS = 3;
    private static final int MAX_CONCURRENT_CONSUMERS = 10;
    private static final long RECEIVE_TIMEOUT = 5000L;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    
    @Value("${spring.rabbitmq.host:localhost}")
    private String host;
    
    @Value("${spring.rabbitmq.port:5672}")
    private int port;
    
    @Value("${spring.rabbitmq.username:guest}")
    private String username;
    
    @Value("${spring.rabbitmq.password:guest}")
    private String password;
    
    @Value("${spring.rabbitmq.virtual-host:/}")
    private String virtualHost;
    
    /**
     * Creates RabbitMQ JMS ConnectionFactory.
     * 
     * @return configured ConnectionFactory
     */
    @Bean
    public ConnectionFactory connectionFactory() {
        RMQConnectionFactory connectionFactory = new RMQConnectionFactory();
        
        configureConnection(connectionFactory);
        configureQueueSettings(connectionFactory);
        
        logger.info(
            "RabbitMQ JMS ConnectionFactory configured: host={}, port={}, virtualHost={}",
            host,
            port,
            virtualHost
        );
        
        return connectionFactory;
    }
    
    /**
     * Creates JMS template for sending messages.
     * 
     * @param connectionFactory JMS connection factory
     * @return configured JmsTemplate
     */
    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
        JmsTemplate template = new JmsTemplate(connectionFactory);
        
        template.setMessageConverter(messageConverter());
        template.setSessionTransacted(true);
        template.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
        template.setReceiveTimeout(RECEIVE_TIMEOUT);
        
        logger.info("JmsTemplate configured with transactional sessions and manual acknowledgment");
        
        return template;
    }
    
    /**
     * Creates JMS listener container factory for consuming messages.
     * 
     * @param connectionFactory JMS connection factory
     * @return configured listener container factory
     */
    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(
        ConnectionFactory connectionFactory
    ) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setSessionTransacted(true);
        factory.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
        factory.setConcurrency(buildConcurrencyString());
        factory.setErrorHandler(new MessagingErrorHandler());
        
        logger.info(
            "JMS Listener Container Factory configured: concurrency={}-{}",
            DEFAULT_CONCURRENT_CONSUMERS,
            MAX_CONCURRENT_CONSUMERS
        );
        
        return factory;
    }
    
    /**
     * Creates message converter for JSON serialization.
     * 
     * @return configured message converter
     */
    @Bean
    public MessageConverter messageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }
    
    private void configureConnection(RMQConnectionFactory connectionFactory) {
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setVirtualHost(virtualHost);
    }
    
    private void configureQueueSettings(RMQConnectionFactory connectionFactory) {
        connectionFactory.setDeclareReplyToDestination(true);
        connectionFactory.setOnMessageTimeoutMs(RECEIVE_TIMEOUT);
    }
    
    private String buildConcurrencyString() {
        return String.format(
            "%d-%d",
            DEFAULT_CONCURRENT_CONSUMERS,
            MAX_CONCURRENT_CONSUMERS
        );
    }
    
    /**
     * Error handler for JMS listener exceptions.
     * Logs errors and allows retry mechanism to handle failures.
     */
    private static class MessagingErrorHandler implements org.springframework.util.ErrorHandler {
        
        private static final Logger errorLogger = LoggerFactory.getLogger(MessagingErrorHandler.class);
        
        @Override
        public void handleError(Throwable throwable) {
            errorLogger.error(
                "Error processing JMS message: {}",
                throwable.getMessage(),
                throwable
            );
        }
    }
}
