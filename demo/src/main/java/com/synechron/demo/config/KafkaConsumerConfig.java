package com.synechron.demo.config;

import com.synechron.demo.model.UserEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, UserEvent> apiConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${app.kafka.consumer.api.group-id}") String groupId,
            @Value("${app.kafka.consumer.api.max-poll-records}") int maxPollRecords,
            @Value("${app.kafka.consumer.api.max-poll-interval-ms}") int maxPollIntervalMs,
            @Value("${app.kafka.consumer.api.session-timeout-ms}") int sessionTimeoutMs,
            @Value("${app.kafka.consumer.api.heartbeat-interval-ms}") int heartbeatIntervalMs) {
        return new DefaultKafkaConsumerFactory<>(baseConsumerConfig(
                bootstrapServers,
                groupId,
                maxPollRecords,
                maxPollIntervalMs,
                sessionTimeoutMs,
                heartbeatIntervalMs));
    }

    @Bean
    public ConsumerFactory<String, UserEvent> batchConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${app.kafka.consumer.batch.group-id}") String groupId,
            @Value("${app.kafka.consumer.batch.max-poll-records}") int maxPollRecords,
            @Value("${app.kafka.consumer.batch.max-poll-interval-ms}") int maxPollIntervalMs,
            @Value("${app.kafka.consumer.batch.session-timeout-ms}") int sessionTimeoutMs,
            @Value("${app.kafka.consumer.batch.heartbeat-interval-ms}") int heartbeatIntervalMs) {
        return new DefaultKafkaConsumerFactory<>(baseConsumerConfig(
                bootstrapServers,
                groupId,
                maxPollRecords,
                maxPollIntervalMs,
                sessionTimeoutMs,
                heartbeatIntervalMs));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserEvent> apiFlowKafkaListenerFactory(
            ConsumerFactory<String, UserEvent> apiConsumerFactory,
            @Value("${app.kafka.consumer.api.concurrency}") int concurrency) {

        ConcurrentKafkaListenerContainerFactory<String, UserEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(apiConsumerFactory);
        factory.setConcurrency(concurrency);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserEvent> batchFlowKafkaListenerFactory(
            ConsumerFactory<String, UserEvent> batchConsumerFactory,
            @Value("${app.kafka.consumer.batch.concurrency}") int concurrency) {

        ConcurrentKafkaListenerContainerFactory<String, UserEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(batchConsumerFactory);
        factory.setConcurrency(concurrency);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    private Map<String, Object> baseConsumerConfig(String bootstrapServers,
                                                    String groupId,
                                                    int maxPollRecords,
                                                    int maxPollIntervalMs,
                                                    int sessionTimeoutMs,
                                                    int heartbeatIntervalMs) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, maxPollIntervalMs);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, sessionTimeoutMs);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, heartbeatIntervalMs);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, UserEvent.class.getName());
        return props;
    }
}
