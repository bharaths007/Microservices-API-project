package com.synechron.User.service;

import com.synechron.User.model.FlowType;
import com.synechron.User.model.User;
import com.synechron.User.model.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventProducerService {

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;

    @Value("${app.kafka.topic.api}")
    private String apiTopic;

    @Value("${app.kafka.topic.batch}")
    private String batchTopic;

    public String publish(User user, FlowType flowType) {
        UserEvent event = UserEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .bic(user.getBic())
                .email(user.getEmail())
                .flowType(flowType)
                .createdAt(Instant.now())
                .build();

        String topic = flowType == FlowType.API ? apiTopic : batchTopic;
        String messageKey = user.getBic();

        kafkaTemplate.send(topic, messageKey, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish eventId={} flowType={} topic={}", event.getEventId(), flowType, topic, ex);
                        return;
                    }
                    log.info("Published eventId={} flowType={} topic={} partition={} offset={}",
                            event.getEventId(),
                            flowType,
                            topic,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                });

        return event.getEventId();
    }
}
