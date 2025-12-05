package com.demo.authservice.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Service responsible for publishing messages to Kafka topics.
 *
 * <p>This class acts as a wrapper around {@link KafkaTemplate} to simplify
 * sending messages from the application.</p>
 *
 * <ul>
 *   <li>Uses Spring’s {@code KafkaTemplate} for asynchronous message delivery.</li>
 *   <li>Provides a simple method to send string-based messages to any topic.</li>
 * </ul>
 */

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String topic, String message) {
        kafkaTemplate.send(topic, message);
    }
}
