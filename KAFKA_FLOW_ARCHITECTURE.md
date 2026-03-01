# Kafka API/Batch Flow Architecture (Long-Term)

## Problem Statement
A single producer serves two workloads:
- API flow: latency-sensitive, high-priority traffic.
- Batch flow: throughput-heavy bulk traffic.

When both workloads share the same consumer pool, batch spikes can increase lag and trigger rebalance loops that affect API TPS.

## High-Level Design (HLD)

### 1) Traffic isolation by topic
Use dedicated topics instead of partition slicing on one topic:
- `user.api.v1`
- `user.batch.v1`

Why:
- independent retention/cleanup policy
- independent scaling and consumer groups
- easier SLO and lag monitoring per flow

### 2) Independent consumer groups and concurrency
- API consumer group: `mail-api-flow-v1`, high concurrency (`15`).
- Batch consumer group: `mail-batch-flow-v1`, lower concurrency (`5`).

This preserves API throughput even during batch bursts.

### 3) Reliability and ordering
- Producer: idempotence enabled, `acks=all`, retries enabled.
- Message key: `bic` to keep ordering per business entity.
- Manual acknowledgment on consumer (`manual_immediate`) so offsets are committed only after processing succeeds.

### 4) Failure strategy and no-death posture
- `DefaultErrorHandler` with bounded retries.
- Failed records are published to dedicated DLT topics:
  - `user.api.v1.dlt`
  - `user.batch.v1.dlt`
- Consumer poll safety tuned (`max.poll.records`, `max.poll.interval.ms`, `session.timeout.ms`, heartbeat interval).

## Low-Level Design (LLD)

### Producer service (User-Service)
- REST endpoints
  - `POST /user/api`
  - `POST /user/batch`
- Build `UserEvent` envelope: eventId, bic, email, flowType, createdAt.
- Route by flow type to dedicated topic.

### Consumer service (demo)
- Two listeners:
  - API listener subscribes to `user.api.v1`.
  - Batch listener subscribes to `user.batch.v1`.
- Both use manual ack and shared processing pipeline.
- DLT handler routes failed records to flow-specific DLT topics.

## Operational Playbook
1. Set topic partition counts aligned with concurrency:
   - API topic partitions >= API consumer concurrency.
   - Batch topic partitions >= Batch concurrency.
2. Monitor:
   - per-topic consumer lag
   - rebalance count
   - processing latency p95/p99
   - DLT inflow rate
3. Capacity guardrails:
   - API backlog alarm: strict threshold and auto-scale.
   - Batch backlog alarm: softer threshold and queue-drain strategy.
4. Replay strategy:
   - build a replay job that reads DLT and republishes with idempotency key (`eventId`).

## Why this is better than static partition ranges in one topic
- no cross-flow starvation
- fewer accidental rebalances from heterogeneous processing times
- simpler ownership model and safer scaling lifecycle
