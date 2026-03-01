# Kafka Flow Isolation Design (HLD + LLD)

## HLD

### Problem Statement
A single Kafka topic carries two workloads:
- API flow: latency-sensitive, high TPS.
- Batch flow: throughput-heavy and bursty.

When both workloads share consumer capacity without isolation, the API flow can suffer lag spikes.

### Long-Term Architecture
1. **Partition affinity at producer**
   - Keep one topic with 20 partitions.
   - Reserve partitions `0-14` for API flow and `15-19` for batch flow.
   - Producer chooses partition deterministically based on flow + stable key.

2. **Workload-isolated consumers**
   - Create two listener containers:
     - API listener reads only partitions `0-14`.
     - Batch listener reads only partitions `15-19`.
   - Separate consumer groups and independent concurrency.

3. **Resilience and anti-lag tuning**
   - Manual acknowledgments.
   - CooperativeStickyAssignor to reduce rebalance impact.
   - Tight poll records for API flow, larger poll records for batch flow.
   - Distinct max poll interval per flow.

4. **Operability and scaling model**
   - Scale API consumer replicas by API partition count.
   - Scale batch consumers independently.
   - Monitor lag per partition range and alert by flow.

## LLD

### Producer App (`User-Service`)
- `PartitionRouter` maps message keys to fixed flow-specific partition ranges.
- `UserEventProducer` publishes `UserMailEvent` with explicit partition.
- API endpoint `/user` publishes API flow events.
- Batch endpoint `/user/batch` publishes batch flow events.

### Consumer App (`demo`)
- `MailEventConsumer.consumeApi` subscribes only to partitions `0-14`.
- `MailEventConsumer.consumeBatch` subscribes only to partitions `15-19`.
- Both listeners use manual ack and independent listener container factories.

### Timeout & Polling Choices
- API flow: lower `max.poll.records` and high enough `max.poll.interval.ms` to prevent false death during transient slowness.
- Batch flow: higher `max.poll.records` and larger `max.poll.interval.ms` for heavier processing.
- `session.timeout.ms` and heartbeat values tuned to avoid frequent unnecessary group evictions.

### Future Hardening Recommendations
- Add retry topic + dead-letter topic.
- Add idempotency store keyed by `eventId`.
- Add outbox pattern if source data must be transactionally consistent with event publication.
- Migrate to **two topics** long-term if workload SLOs diverge significantly (`mail-events-api`, `mail-events-batch`).
