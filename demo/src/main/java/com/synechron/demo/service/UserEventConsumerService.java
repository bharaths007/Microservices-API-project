package com.synechron.demo.service;

import com.synechron.demo.model.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventConsumerService {

    private final MailService mailService;

    @KafkaListener(
            id = "api-flow-consumer",
            topics = "${app.kafka.topic.api}",
            groupId = "${app.kafka.consumer.group-api}",
            concurrency = "${app.kafka.consumer.api-concurrency}")
    public void consumeApi(UserEvent event, Acknowledgment acknowledgment) {
        process(event, acknowledgment);
    }

    @KafkaListener(
            id = "batch-flow-consumer",
            topics = "${app.kafka.topic.batch}",
            groupId = "${app.kafka.consumer.group-batch}",
            concurrency = "${app.kafka.consumer.batch-concurrency}")
    public void consumeBatch(UserEvent event, Acknowledgment acknowledgment) {
        process(event, acknowledgment);
    }

    private void process(UserEvent event, Acknowledgment acknowledgment) {
        long start = System.currentTimeMillis();
        mailService.mailSend(event.getBic() + " " + event.getEmail());
        acknowledgment.acknowledge();
        log.info("Processed eventId={} flow={} in {} ms", event.getEventId(), event.getFlowType(), System.currentTimeMillis() - start);
    }
}
