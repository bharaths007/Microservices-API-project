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
            topics = "${app.kafka.topic.mail-events-api}",
            containerFactory = "apiKafkaListenerContainerFactory"
    )
    public void consumeApi(UserMailEvent event, Acknowledgment acknowledgment) {
        mailEventFlowRouter.process(event);
        acknowledgment.acknowledge();
    }

    @KafkaListener(
            topics = "${app.kafka.topic.mail-events-batch}",
            containerFactory = "batchKafkaListenerContainerFactory"
    )
    public void consumeBatch(UserMailEvent event, Acknowledgment acknowledgment) {
        mailEventFlowRouter.process(event);
        acknowledgment.acknowledge();
    }
}
