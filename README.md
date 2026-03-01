# Microservices-API-project

Communication between Microservices and implementing a mail sender service.

## Kafka Flow Isolation Upgrade
This repository now includes a long-term Kafka design to isolate API and Batch traffic:

- Producer app (`User-Service`) publishes to two topics:
  - `user-events-api` (15 partitions)
  - `user-events-batch` (5 partitions)
- Consumer app (`demo`) consumes with separate consumer groups and listener settings per flow.

Design details are documented in [`docs/kafka-flow-isolation-design.md`](docs/kafka-flow-isolation-design.md).
