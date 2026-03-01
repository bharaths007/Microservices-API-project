package com.synechron.User.kafka;

import com.synechron.User.model.User;
import com.synechron.User.model.kafka.FlowType;
import com.synechron.User.model.kafka.UserMailEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class UserEventProducer {

    private final KafkaTemplate<String, UserMailEvent> kafkaTemplate;
    private final String apiTopic;
    private final String batchTopic;

    public UserEventProducer(
            KafkaTemplate<String, UserMailEvent> kafkaTemplate,
            @Value("${app.kafka.topic.mail-events-api}") String apiTopic,
            @Value("${app.kafka.topic.mail-events-batch}") String batchTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.apiTopic = apiTopic;
        this.batchTopic = batchTopic;
    }

    public void publish(User user, FlowType flowType) {
        String stableKey = user.getEmail();
        String topic = flowType == FlowType.API ? apiTopic : batchTopic;
        UserMailEvent event = new UserMailEvent(
                UUID.randomUUID().toString(),
                Instant.now().toEpochMilli(),
                flowType,
                user
        );

        kafkaTemplate.send(topic, stableKey, event);
    }
}
