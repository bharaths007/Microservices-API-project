package com.synechron.demo.kafka;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.kafka")
public class KafkaFlowProperties {

    @NotBlank
    private String topic;

    @Min(1)
    private int totalPartitions;

    @Min(1)
    private int batchPartitionCount;

    @Min(1)
    private int apiConsumerConcurrency = 15;

    @Min(1)
    private int batchConsumerConcurrency = 5;

    @Min(1)
    private int maxPollRecords = 200;

    @Min(1000)
    private int maxPollIntervalMs = 300000;

    @Min(1)
    private int sessionTimeoutMs = 45000;

    @Min(1)
    private int heartbeatIntervalMs = 15000;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getTotalPartitions() {
        return totalPartitions;
    }

    public void setTotalPartitions(int totalPartitions) {
        this.totalPartitions = totalPartitions;
    }

    public int getBatchPartitionCount() {
        return batchPartitionCount;
    }

    public void setBatchPartitionCount(int batchPartitionCount) {
        this.batchPartitionCount = batchPartitionCount;
    }

    public int getApiConsumerConcurrency() {
        return apiConsumerConcurrency;
    }

    public void setApiConsumerConcurrency(int apiConsumerConcurrency) {
        this.apiConsumerConcurrency = apiConsumerConcurrency;
    }

    public int getBatchConsumerConcurrency() {
        return batchConsumerConcurrency;
    }

    public void setBatchConsumerConcurrency(int batchConsumerConcurrency) {
        this.batchConsumerConcurrency = batchConsumerConcurrency;
    }

    public int getMaxPollRecords() {
        return maxPollRecords;
    }

    public void setMaxPollRecords(int maxPollRecords) {
        this.maxPollRecords = maxPollRecords;
    }

    public int getMaxPollIntervalMs() {
        return maxPollIntervalMs;
    }

    public void setMaxPollIntervalMs(int maxPollIntervalMs) {
        this.maxPollIntervalMs = maxPollIntervalMs;
    }

    public int getSessionTimeoutMs() {
        return sessionTimeoutMs;
    }

    public void setSessionTimeoutMs(int sessionTimeoutMs) {
        this.sessionTimeoutMs = sessionTimeoutMs;
    }

    public int getHeartbeatIntervalMs() {
        return heartbeatIntervalMs;
    }

    public void setHeartbeatIntervalMs(int heartbeatIntervalMs) {
        this.heartbeatIntervalMs = heartbeatIntervalMs;
    }
}
