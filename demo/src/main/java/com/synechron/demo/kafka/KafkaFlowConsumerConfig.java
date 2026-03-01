package com.synechron.demo.kafka;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

@Configuration
@EnableConfigurationProperties(KafkaFlowProperties.class)
public class KafkaFlowConsumerConfig {

    @Bean
    public FlowPartitionPlanner flowPartitionPlanner() {
        return new FlowPartitionPlanner();
    }

    @Bean
    public ConsumerFactory<String, String> flowConsumerFactory(KafkaFlowProperties flowProperties, KafkaProperties kafkaProperties) {
        Map<String, Object> config = kafkaProperties.buildConsumerProperties();
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, flowProperties.getMaxPollRecords());
        config.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, flowProperties.getMaxPollIntervalMs());
        config.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, flowProperties.getSessionTimeoutMs());
        config.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, flowProperties.getHeartbeatIntervalMs());
        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public CommonErrorHandler flowErrorHandler() {
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(5);
        backOff.setInitialInterval(500L);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(5000L);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(backOff);
        errorHandler.setAckAfterHandle(false);
        return errorHandler;
    }

    @Bean
    public KafkaMessageListenerContainer<String, String> apiFlowContainer(
            ConsumerFactory<String, String> flowConsumerFactory,
            KafkaFlowProperties properties,
            FlowPartitionPlanner planner,
            FlowRecordHandler handler,
            CommonErrorHandler errorHandler) {
        return createFlowContainer(flowConsumerFactory, properties, planner, handler, errorHandler, FlowType.API, "api-flow-group");
    }

    @Bean
    public KafkaMessageListenerContainer<String, String> batchFlowContainer(
            ConsumerFactory<String, String> flowConsumerFactory,
            KafkaFlowProperties properties,
            FlowPartitionPlanner planner,
            FlowRecordHandler handler,
            CommonErrorHandler errorHandler) {
        return createFlowContainer(flowConsumerFactory, properties, planner, handler, errorHandler, FlowType.BATCH, "batch-flow-group");
    }

    private KafkaMessageListenerContainer<String, String> createFlowContainer(
            ConsumerFactory<String, String> flowConsumerFactory,
            KafkaFlowProperties properties,
            FlowPartitionPlanner planner,
            FlowRecordHandler handler,
            CommonErrorHandler errorHandler,
            FlowType flowType,
            String groupId) {

        List<Integer> partitions = planner
                .planPartitions(properties.getTotalPartitions(), properties.getBatchPartitionCount())
                .get(flowType);

        TopicPartition[] topicPartitions = partitions
                .stream()
                .map(partition -> new TopicPartition(properties.getTopic(), partition))
                .toArray(TopicPartition[]::new);

        ContainerProperties containerProperties = new ContainerProperties(topicPartitions);
        containerProperties.setGroupId(groupId);
        containerProperties.setAckMode(ContainerProperties.AckMode.RECORD);
        containerProperties.setPollTimeout(Duration.ofSeconds(3).toMillis());
        containerProperties.setMessageListener((MessageListener<String, String>) record -> handler.handle(flowType, record));

        KafkaMessageListenerContainer<String, String> container =
                new KafkaMessageListenerContainer<>(flowConsumerFactory, containerProperties);
        container.setCommonErrorHandler(errorHandler);
        return container;
    }
}
