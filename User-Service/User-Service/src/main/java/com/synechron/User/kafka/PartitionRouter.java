package com.synechron.User.kafka;

import com.synechron.User.model.kafka.FlowType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PartitionRouter {

    private final int apiPartitionStart;
    private final int apiPartitionCount;
    private final int batchPartitionStart;
    private final int batchPartitionCount;

    public PartitionRouter(
            @Value("${app.kafka.api-flow.partition-start:0}") int apiPartitionStart,
            @Value("${app.kafka.api-flow.partition-count:15}") int apiPartitionCount,
            @Value("${app.kafka.batch-flow.partition-start:15}") int batchPartitionStart,
            @Value("${app.kafka.batch-flow.partition-count:5}") int batchPartitionCount) {
        this.apiPartitionStart = apiPartitionStart;
        this.apiPartitionCount = apiPartitionCount;
        this.batchPartitionStart = batchPartitionStart;
        this.batchPartitionCount = batchPartitionCount;
    }

    public int resolvePartition(String stableKey, FlowType flowType) {
        int hash = Math.abs(stableKey.hashCode());
        if (flowType == FlowType.API) {
            return apiPartitionStart + (hash % apiPartitionCount);
        }
        return batchPartitionStart + (hash % batchPartitionCount);
    }
}
