package com.synechron.demo.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface FlowRecordHandler {
    void handle(FlowType flowType, ConsumerRecord<String, String> record);
}
