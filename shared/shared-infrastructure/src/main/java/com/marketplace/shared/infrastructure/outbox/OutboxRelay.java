package com.marketplace.shared.infrastructure.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelay {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 2000) // Poll every 2 seconds
    @Transactional
    public void processOutbox() {
        List<OutboxEvent> events = outboxEventRepository.findUnprocessed(PageRequest.of(0, 50));

        if (events.isEmpty()) {
            return;
        }

        log.debug("Found {} unprocessed outbox events", events.size());

        for (OutboxEvent event : events) {
            try {
                // Topic naming convention: marketplace.events.<EventType>
                // OR generic topic: marketplace.domain-events
                String topic = "marketplace.domain-events"; 
                String key = event.getAggregateId();

                kafkaTemplate.send(topic, key, event.getPayload()).get(); // Sync send for safety in this loop
                
                event.setProcessed(true);
                outboxEventRepository.save(event);
                
                log.debug("Published event {} to Kafka topic {}", event.getId(), topic);
            } catch (Exception e) {
                log.error("Failed to publish event {} to Kafka", event.getId(), e);
                // Will retry on next poll (transaction rollback or just skip)
            }
        }
    }
}
