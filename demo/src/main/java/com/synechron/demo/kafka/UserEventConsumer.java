package com.synechron.demo.kafka;

import com.synechron.demo.model.UserEvent;
import com.synechron.demo.service.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class UserEventConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserEventConsumer.class);

    private final MailService mailService;

    public UserEventConsumer(MailService mailService) {
        this.mailService = mailService;
    }

    @KafkaListener(topics = "${app.kafka.topics.api}", containerFactory = "apiFlowKafkaListenerFactory")
    public void consumeApiFlow(UserEvent event, Acknowledgment acknowledgment) {
        processAndAcknowledge("API", event, acknowledgment);
    }

    @KafkaListener(topics = "${app.kafka.topics.batch}", containerFactory = "batchFlowKafkaListenerFactory")
    public void consumeBatchFlow(UserEvent event, Acknowledgment acknowledgment) {
        processAndAcknowledge("BATCH", event, acknowledgment);
    }

    private void processAndAcknowledge(String flow, UserEvent event, Acknowledgment acknowledgment) {
        try {
            mailService.mailSend(event.getBic() + " " + event.getEmail());
            acknowledgment.acknowledge();
        } catch (Exception ex) {
            LOGGER.error("{} flow processing failed for eventId={}", flow, event.getEventId(), ex);
            throw ex;
        }
    }
}
