# Kafka Flow Isolation Design (HLD + LLD)

## 1) Overview
This design isolates API and Batch execution while keeping one deployable consumer service per product.

## 2) Problem
API traffic must remain low-latency even during heavy batch backlogs.
Topic separation alone is not enough if both flows still share one consumer group/poll loop.

## 3) Design goals
- True flow-level execution isolation
- Independent scaling and tuning
- Minimal operational overhead
- Multi-product reusability

## 4) HLD

### 4.1 Topic Isolation
Per product, use two topics:
- `<product>-events-api`
- `<product>-events-batch`

### 4.2 Consumer Group Isolation (critical)
Use separate consumer groups:
- `<product>-api-consumer-group`
- `<product>-batch-consumer-group`

This gives independent:
- poll loops
- offsets
- rebalances
- scaling

### 4.3 Logical flow
Producer routes by `flowType`:
- API -> `<product>-events-api`
- BATCH -> `<product>-events-batch`

Single consumer application hosts two listeners:
- API listener -> API group
- Batch listener -> Batch group

### 4.4 Scaling profile
- API: higher partitions and concurrency for low latency
- Batch: controlled concurrency for throughput and stability

## 5) LLD (implemented)

### Producer (`User-Service`)
- `UserEventProducer` routes API/BATCH events to separate topics.
- Producer reliability stays hardened (`acks=all`, idempotence, retries).

### Consumer (`demo`)
- `KafkaConsumerConfig` defines:
  - `apiConsumerFactory` + `apiKafkaListenerContainerFactory`
  - `batchConsumerFactory` + `batchKafkaListenerContainerFactory`
- `MailEventConsumer` has two listeners (API topic and Batch topic).
- `MailEventFlowRouter` keeps flow-specific processing centralized.

## 6) Multi-product pattern
For each product, repeat naming convention:
- `p1-events-api`, `p1-events-batch`
- `p1-api-consumer-group`, `p1-batch-consumer-group`

Same framework, different product config.

## 7) Enhancements recommended
- Retry/DLT per flow
- Idempotency by `eventId`
- Lag alerts and SLO dashboards split by flow
- Optional pause/resume batch listener under pressure
