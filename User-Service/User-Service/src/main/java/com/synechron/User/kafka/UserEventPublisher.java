package com.synechron.User.kafka;

import com.synechron.User.model.User;
import com.synechron.User.model.UserEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserEventPublisher {

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;
    private final String apiTopic;
    private final String batchTopic;

    public UserEventPublisher(KafkaTemplate<String, UserEvent> kafkaTemplate,
                              @Value("${app.kafka.topics.api}") String apiTopic,
                              @Value("${app.kafka.topics.batch}") String batchTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.apiTopic = apiTopic;
        this.batchTopic = batchTopic;
    }

    public void publishApi(User user) {
        UserEvent event = UserEvent.of("API", user);
        kafkaTemplate.send(apiTopic, user.getBic(), event);
    }

    public void publishBatch(User user) {
        UserEvent event = UserEvent.of("BATCH", user);
        kafkaTemplate.send(batchTopic, user.getBic(), event);
    }
}
