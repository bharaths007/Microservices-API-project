package com.synechron.User.kafka;

import com.synechron.User.model.kafka.FlowType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PartitionRouterTest {

    private final PartitionRouter partitionRouter = new PartitionRouter(0, 15, 15, 5);

    @Test
    void shouldMapApiFlowInsideApiPartitionRange() {
        int partition = partitionRouter.resolvePartition("test-api@example.com", FlowType.API);
        assertTrue(partition >= 0 && partition <= 14);
    }

    @Test
    void shouldMapBatchFlowInsideBatchPartitionRange() {
        int partition = partitionRouter.resolvePartition("batch@example.com", FlowType.BATCH);
        assertTrue(partition >= 15 && partition <= 19);
    }
}
