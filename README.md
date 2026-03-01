# Microservices-API-project
Communication between Microservices and Implementing Mail Sender Service

## Kafka-based flow isolation

This repository now contains a long-term design and implementation to isolate API and Batch flows over Kafka using **topic-level separation with dedicated consumer groups per flow** (true execution isolation) while keeping a single deployable consumer service per product.

- Design document: `docs/kafka-flow-design.md`
- Producer implementation: `User-Service`
- Consumer implementation: `demo`
