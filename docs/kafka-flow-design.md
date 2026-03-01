# Kafka Flow Isolation Design (HLD + LLD)

## HLD

### Problem Statement
You need API and batch workloads to coexist without lag spikes, while also keeping the solution easy to operate when more products are added.

### Proposed Long-Term Architecture (maintainable)
1. **Flow isolation by topic, not by extra consumer components**
   - Use two topics per product:
     - `<product>-events-api`
     - `<product>-events-batch`
   - Producer routes by `flowType` to the topic.

2. **Single consumer component per product**
   - One `@KafkaListener` subscribes to both flow topics.
   - A lightweight in-process router dispatches events to flow-specific handlers.
   - This avoids creating separate apps/containers/listeners for each flow.

3. **Scale and reliability controls**
   - Keep one consumer group per product.
   - Tune concurrency and poll limits centrally.
   - Use manual acknowledgments and cooperative sticky assignment.

4. **Multi-product pattern**
   - Reuse the same framework for each product:
     - Producer topic names: `p1-events-api`, `p1-events-batch`, `p2-events-api`, `p2-events-batch`.
     - Consumer code remains mostly identical (routing + handler strategy).

## LLD implemented in this repo

### Producer (`User-Service`)
- `UserEventProducer` now routes events to `mail-events-api` or `mail-events-batch` based on `flowType`.
- Delivery settings remain resilient (`acks=all`, idempotence, retries, batching).

### Consumer (`demo`)
- `KafkaConsumerConfig` provides one reusable listener container factory with shared hardening defaults.
- `MailEventConsumer` is a single listener method subscribed to both topics.
- `MailEventFlowRouter` handles flow-specific branching in one place.

## Why this is easier to maintain
- No partition pinning tables in code.
- No duplicated listener infrastructure per flow.
- Adding a new product reuses the same pattern with topic naming + small handler additions.

## Operational recommendations
- Keep API topic partition count higher than batch when API latency is critical.
- Alert separately on lag for API and batch topics.
- Add retry + DLQ topics for poison messages.
- Add idempotency checks using `eventId`.
