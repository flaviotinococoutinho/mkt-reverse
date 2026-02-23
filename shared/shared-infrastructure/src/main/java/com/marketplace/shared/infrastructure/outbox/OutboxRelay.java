package com.marketplace.shared.infrastructure.outbox;

import com.marketplace.shared.infrastructure.messaging.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelay {

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedDelay = 2000) // Poll every 2 seconds
    @SchedulerLock(name = "outboxRelayLock", lockAtMostFor = "1m", lockAtLeastFor = "1s")
    @Transactional
    public void processOutbox() {
        List<OutboxEvent> events = outboxEventRepository.findUnprocessed(PageRequest.of(0, 50));

        if (events.isEmpty()) {
            return;
        }

        log.debug("Found {} unprocessed outbox events", events.size());

        for (OutboxEvent event : events) {
            try {
                // Dynamic routing key based on aggregate type and event type
                String aggregateType = event.getAggregateType() != null ? event.getAggregateType().toLowerCase() : "unknown";
                String eventType = event.getEventType() != null ? event.getEventType().toLowerCase() : "unknown";
                String routingKey = "domain." + aggregateType + "." + eventType;

                // Send JSON payload to the specific exchange with dynamic routing key
                rabbitTemplate.convertAndSend(
                    RabbitMqConfig.DOMAIN_EXCHANGE,
                    routingKey,
                    event.getPayload()
                );
                
                event.setProcessed(true);
                outboxEventRepository.save(event);
                
                log.debug("Published event {} to RabbitMQ exchange {} with routingKey {}", event.getId(), RabbitMqConfig.DOMAIN_EXCHANGE, routingKey);
            } catch (Exception e) {
                log.error("Failed to publish event {} to RabbitMQ", event.getId(), e);
                // Will retry on next poll (transaction rollback or just skip)
            }
        }
    }
}
