# Kafka Flow-Isolated Consumer Design (HLD + LLD)

## Problem summary
A single topic contains mixed traffic with 2 flows:
- API flow (latency-sensitive, high TPS)
- Batch flow (throughput-oriented, bursty)

If both flows share consumer group assignment dynamically, a batch spike can starve API processing, increase lag, and cause consumer group churn/rebalance deaths.

## HLD (long-term architecture)

### 1) Topic + partition ownership model
- Keep one topic with fixed partition budget (`N=20`) only if ordering rules across flows require a shared topic.
- Reserve a static partition slice per flow at producer side and consumer side:
  - API flow: partitions `0..14`
  - Batch flow: partitions `15..19`
- Run **separate consumer groups** for each flow:
  - `api-flow-group`
  - `batch-flow-group`

This isolates lag, retry storms, and scaling behavior between flows.

### 2) Consumer failure containment
- Use dedicated listener containers per flow with fixed partition assignment.
- Use record-level ack and bounded retry with exponential backoff.
- Route poison messages to DLQ (recommended extension) after retry exhaustion.

### 3) Throughput/latency tuning controls
- `max.poll.records`: bound per-poll work so processing always completes before `max.poll.interval.ms`.
- `session.timeout.ms` and heartbeat tuned to avoid false consumer death under transient pauses.
- Scale API and batch consumers independently by concurrency + instance count.

### 4) Operability SLOs
Track per-flow metrics:
- consumer lag per partition
- records-consumed-rate
- rebalance count
- processing duration p95/p99
- DLQ rate

Alert independently for API and batch groups.

## LLD (implementation in this repo)

Implemented classes in `demo` service:
- `FlowPartitionPlanner`: deterministic partition split logic.
- `KafkaFlowProperties`: validated config for partition + poll/timeout tuning.
- `KafkaFlowConsumerConfig`: builds isolated Kafka listener containers:
  - explicit partition assignment by flow
  - dedicated group ids
  - shared retry/error strategy with exponential backoff
- `FlowRecordHandler` + `LoggingFlowRecordHandler`: flow-specific processing hook.

## Runtime sizing formula
For each flow:

`max_processing_time_per_poll < max.poll.interval.ms * 0.7`

Use:
- `max.poll.records = floor(target_processing_budget_ms / avg_record_processing_ms)`
- Increase consumer instances before increasing `max.poll.records` when lag grows.

## Production hardening recommendations (next step)
1. Add DLQ topic per flow (`<topic>.api.dlq`, `<topic>.batch.dlq`).
2. Add idempotency key check to avoid duplicate side effects.
3. Add autoscaling using lag-based policy per flow.
4. Prefer CooperativeStickyAssignor if moving back to subscription mode.
5. If strict isolation is mandatory, split into 2 topics (API + Batch) and keep current partition budget logic.
