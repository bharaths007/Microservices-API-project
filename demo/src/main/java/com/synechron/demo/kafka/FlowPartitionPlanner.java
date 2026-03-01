package com.synechron.demo.kafka;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class FlowPartitionPlanner {

    public Map<FlowType, List<Integer>> planPartitions(int totalPartitions, int batchPartitionCount) {
        if (batchPartitionCount <= 0 || totalPartitions <= 0 || batchPartitionCount >= totalPartitions) {
            throw new IllegalArgumentException("batchPartitionCount must be between 1 and totalPartitions - 1");
        }

        int apiPartitionCount = totalPartitions - batchPartitionCount;

        List<Integer> apiPartitions = IntStream.range(0, apiPartitionCount)
                .boxed()
                .toList();

        List<Integer> batchPartitions = IntStream.range(apiPartitionCount, totalPartitions)
                .boxed()
                .toList();

        Map<FlowType, List<Integer>> flowPartitions = new EnumMap<>(FlowType.class);
        flowPartitions.put(FlowType.API, apiPartitions);
        flowPartitions.put(FlowType.BATCH, batchPartitions);

        return flowPartitions;
    }
}
