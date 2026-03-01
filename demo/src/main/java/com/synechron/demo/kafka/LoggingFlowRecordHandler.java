package com.synechron.demo.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoggingFlowRecordHandler implements FlowRecordHandler {

    @Override
    public void handle(FlowType flowType, ConsumerRecord<String, String> record) {
        log.info(
                "flow={}, topic={}, partition={}, offset={}, key={}, payload={}",
                flowType,
                record.topic(),
                record.partition(),
                record.offset(),
                record.key(),
                record.value());
    }
}
