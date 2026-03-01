package com.synechron.demo.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class FlowPartitionPlannerTest {

    private final FlowPartitionPlanner planner = new FlowPartitionPlanner();

    @Test
    void shouldPlanApiAndBatchPartitionsDeterministically() {
        Map<FlowType, List<Integer>> partitions = planner.planPartitions(20, 5);

        assertEquals(15, partitions.get(FlowType.API).size());
        assertEquals(5, partitions.get(FlowType.BATCH).size());
        assertEquals(List.of(0, 1, 2, 3, 4), partitions.get(FlowType.API).subList(0, 5));
        assertEquals(List.of(15, 16, 17, 18, 19), partitions.get(FlowType.BATCH));
    }

    @Test
    void shouldRejectInvalidBatchPartitionCount() {
        assertThrows(IllegalArgumentException.class, () -> planner.planPartitions(20, 0));
        assertThrows(IllegalArgumentException.class, () -> planner.planPartitions(20, 20));
    }
}
