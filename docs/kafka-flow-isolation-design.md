# Kafka Flow Isolation Design (HLD + LLD)

## 1. Problem Statement
You have two traffic types sharing Kafka:
- **API flow**: low latency, high TPS, user-facing.
- **Batch flow**: heavy bulk traffic.

When both share the same consumer group and partitions, batch spikes can create lag and rebalance pressure that hurts API SLAs.

## 2. Long-term HLD

### 2.1 Core Design
Use **topic-level isolation** instead of partition-level isolation in one topic:
- `user-events-api` (15 partitions)
- `user-events-batch` (5 partitions)

This keeps throughput, lag, scaling, and failure domains independent.

### 2.2 Producer Strategy
- Producer chooses topic based on flow (`/user/api`, `/user/batch`).
- Use `acks=all`, `enable.idempotence=true`, retries, compression and batching.
- Key by `bic` for ordering per business key.

### 2.3 Consumer Strategy
- Separate consumer groups:
  - `mail-consumer-api`
  - `mail-consumer-batch`
- Separate listener factories and per-flow tuning:
  - API: lower poll interval, high concurrency, smaller processing window.
  - Batch: larger poll interval and poll records for heavy processing.
- Manual ack after successful business processing.

### 2.4 Reliability and Lag Control
- Keep processing idempotent.
- Ack only after success.
- Use separate autoscaling per flow based on lag metrics.
- Configure DLQ/retry topic in future iteration for poison-pill handling.
- Avoid long blocking work in Kafka listener thread; move expensive I/O to bounded worker pools if needed.

## 3. LLD Mapping to This Repository

### 3.1 User-Service (Producer)
- `UserController` exposes:
  - `POST /user/api`
  - `POST /user/batch`
- `UserEventPublisher` sends to topic by flow.
- `KafkaProducerConfig` enables idempotent and durable producer behavior.
- `KafkaTopicConfig` auto-creates API and Batch topics with separate partition counts.

### 3.2 demo (Consumer)
- `UserEventConsumer` has independent listeners for API and Batch topics.
- `KafkaConsumerConfig` provides two listener container factories with different group IDs and polling/timeouts.
- Ack mode is `MANUAL_IMMEDIATE` and acknowledgment happens only after mail send succeeds.

## 4. Capacity Planning Guardrails
- API partitions should be >= max expected API consumer concurrency.
- Batch partitions sized by batch SLA, not by API requirements.
- Start with:
  - API: 15 partitions, 15 consumer threads
  - Batch: 5 partitions, 5 consumer threads
- Track and alert on:
  - consumer lag by topic/group
  - rebalance count
  - processing latency p95/p99
  - listener exception rate

## 5. Why this is Better than Single-topic Partition Pinning
- No contention between API and Batch backlog.
- Rebalance and lag isolated per flow.
- Simpler scaling and operational ownership.
- Easier to tune `max.poll.records`, `max.poll.interval.ms`, and concurrency independently.
