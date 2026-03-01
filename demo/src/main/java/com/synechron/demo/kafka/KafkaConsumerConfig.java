package com.synechron.demo.kafka;

import com.synechron.demo.model.kafka.UserMailEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${app.kafka.consumer.api.group-id:mail-api-consumer-group}")
    private String apiGroupId;

    @Value("${app.kafka.consumer.batch.group-id:mail-batch-consumer-group}")
    private String batchGroupId;

    @Value("${app.kafka.consumer.api.concurrency:6}")
    private int apiConcurrency;

    @Value("${app.kafka.consumer.batch.concurrency:2}")
    private int batchConcurrency;

    @Bean
    public ConsumerFactory<String, UserMailEvent> apiConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(baseConsumerConfig(apiGroupId, 50, 300000));
    }

    @Bean
    public ConsumerFactory<String, UserMailEvent> batchConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(baseConsumerConfig(batchGroupId, 200, 900000));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserMailEvent> apiKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UserMailEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(apiConsumerFactory());
        factory.setConcurrency(apiConcurrency);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.getContainerProperties().setAsyncAcks(true);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserMailEvent> batchKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UserMailEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(batchConsumerFactory());
        factory.setConcurrency(batchConcurrency);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.getContainerProperties().setAsyncAcks(true);
        return factory;
    }

    private Map<String, Object> baseConsumerConfig(String groupId, int maxPollRecords, int maxPollIntervalMs) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, UserMailEvent.class.getName());
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.synechron.demo.model.kafka,com.synechron.demo.model");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, maxPollIntervalMs);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 20000);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 6000);
        props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG,
                "org.apache.kafka.clients.consumer.CooperativeStickyAssignor");
        return props;
    }
}
