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
    private final PartitionRouter partitionRouter;
    private final String topic;

    public UserEventProducer(
            KafkaTemplate<String, UserMailEvent> kafkaTemplate,
            PartitionRouter partitionRouter,
            @Value("${app.kafka.topic.mail-events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.partitionRouter = partitionRouter;
        this.topic = topic;
    }

    public void publish(User user, FlowType flowType) {
        String stableKey = user.getEmail();
        int partition = partitionRouter.resolvePartition(stableKey, flowType);
        UserMailEvent event = new UserMailEvent(
                UUID.randomUUID().toString(),
                Instant.now().toEpochMilli(),
                flowType,
                user
        );

        kafkaTemplate.send(topic, partition, stableKey, event);
    }
}
