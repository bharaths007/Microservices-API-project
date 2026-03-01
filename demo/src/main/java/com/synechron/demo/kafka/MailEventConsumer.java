package com.synechron.demo.kafka;

import com.synechron.demo.model.UserDto;
import com.synechron.demo.model.kafka.UserMailEvent;
import com.synechron.demo.service.MailService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class MailEventConsumer {

    private final MailService mailService;

    public MailEventConsumer(MailService mailService) {
        this.mailService = mailService;
    }

    @KafkaListener(
            topicPartitions = @TopicPartition(topic = "${app.kafka.topic.mail-events}", partitions = {
                    "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14"
            }),
            containerFactory = "apiKafkaListenerContainerFactory"
    )
    public void consumeApi(UserMailEvent event, Acknowledgment acknowledgment) {
        processEvent(event);
        acknowledgment.acknowledge();
    }

    @KafkaListener(
            topicPartitions = @TopicPartition(topic = "${app.kafka.topic.mail-events}", partitions = {
                    "15", "16", "17", "18", "19"
            }),
            containerFactory = "batchKafkaListenerContainerFactory"
    )
    public void consumeBatch(UserMailEvent event, Acknowledgment acknowledgment) {
        processEvent(event);
        acknowledgment.acknowledge();
    }

    private void processEvent(UserMailEvent event) {
        UserDto payload = event.payload();
        mailService.mailSend(payload.getBic() + " " + payload.getEmail());
    }
}
