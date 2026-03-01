package com.synechron.User.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic apiFlowTopic(@Value("${app.kafka.topics.api}") String topic,
                                 @Value("${app.kafka.partitions.api}") int partitions,
                                 @Value("${app.kafka.replication-factor:1}") short replicationFactor) {
        return TopicBuilder.name(topic)
                .partitions(partitions)
                .replicas(replicationFactor)
                .build();
    }

    @Bean
    public NewTopic batchFlowTopic(@Value("${app.kafka.topics.batch}") String topic,
                                   @Value("${app.kafka.partitions.batch}") int partitions,
                                   @Value("${app.kafka.replication-factor:1}") short replicationFactor) {
        return TopicBuilder.name(topic)
                .partitions(partitions)
                .replicas(replicationFactor)
                .build();
    }
}
