package com.skillswap.exchange_service.kafka;

import com.skillswap.exchange_service.entity.Exchange;
import com.skillswap.exchange_service.event.ExchangeStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ExchangeEventProducer {

    private static final Logger log = LoggerFactory.getLogger(ExchangeEventProducer.class);

    private final KafkaTemplate<String, ExchangeStatusChangedEvent> kafkaTemplate;
    private final String topic;

    public ExchangeEventProducer(
            KafkaTemplate<String, ExchangeStatusChangedEvent> kafkaTemplate,
            @Value("${skillswap.kafka.topics.exchange-status-changed:exchange.status.changed}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publishStatusChanged(Exchange exchange) {
        ExchangeStatusChangedEvent event = new ExchangeStatusChangedEvent(
                exchange.getId(),
                exchange.getRequesterId(),
                exchange.getProviderId(),
                exchange.getSkillId(),
                exchange.getSkillTitle(),
                exchange.getStatus(),
                exchange.getProposedDateTime(),
                exchange.getMeetingLink(),
                Instant.now()
        );

        String key = String.valueOf(exchange.getId());

        kafkaTemplate.send(topic, key, event).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Échec publication Kafka sur [{}] pour l'échange {} (statut={}) : {}",
                        topic, exchange.getId(), exchange.getStatus(), ex.getMessage());
            } else if (result != null) {
                log.info("Événement Kafka publié sur [{}] partition={} offset={} clé={} statut={}",
                        topic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        key,
                        exchange.getStatus());
            }
        });
    }
}