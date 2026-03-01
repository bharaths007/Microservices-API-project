package com.synechron.User.kafka;

import com.synechron.User.model.User;
import com.synechron.User.model.kafka.FlowType;
import com.synechron.User.model.kafka.UserMailEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class UserEventProducerTest {

    private KafkaTemplate<String, UserMailEvent> kafkaTemplate;
    private UserEventProducer userEventProducer;

    @BeforeEach
    void setup() {
        kafkaTemplate = mock(KafkaTemplate.class);
        userEventProducer = new UserEventProducer(kafkaTemplate, "mail-events-api", "mail-events-batch");
    }

    @Test
    void shouldPublishApiFlowToApiTopic() {
        User user = new User("BIC-1", "api@example.com");

        userEventProducer.publish(user, FlowType.API);

        verify(kafkaTemplate).send(eq("mail-events-api"), eq("api@example.com"), org.mockito.ArgumentMatchers.any(UserMailEvent.class));
    }

    @Test
    void shouldPublishBatchFlowToBatchTopic() {
        User user = new User("BIC-2", "batch@example.com");

        userEventProducer.publish(user, FlowType.BATCH);

        ArgumentCaptor<UserMailEvent> eventCaptor = ArgumentCaptor.forClass(UserMailEvent.class);
        verify(kafkaTemplate).send(eq("mail-events-batch"), eq("batch@example.com"), eventCaptor.capture());
        assertEquals(FlowType.BATCH, eventCaptor.getValue().flowType());
    }
}
