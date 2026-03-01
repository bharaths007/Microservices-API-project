package com.synechron.demo.kafka;

import com.synechron.demo.model.kafka.UserMailEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class MailEventConsumer {

    private final MailEventFlowRouter mailEventFlowRouter;

    public MailEventConsumer(MailEventFlowRouter mailEventFlowRouter) {
        this.mailEventFlowRouter = mailEventFlowRouter;
    }

    @KafkaListener(
            topics = {"${app.kafka.topic.mail-events-api}", "${app.kafka.topic.mail-events-batch}"},
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(UserMailEvent event, Acknowledgment acknowledgment) {
        mailEventFlowRouter.process(event);
        acknowledgment.acknowledge();
    }
}
