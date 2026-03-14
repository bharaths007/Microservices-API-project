# Distributed Rate Limiter (Token Bucket)

Sample project for a **distributed API gateway-style rate limiter** using:

- Java 17
- Spring Boot 3
- Redis (shared state across instances)
- Lua script (atomic token-bucket operations)

## How it works

Each request sends an `X-Client-Id` header.

1. App builds Redis keys for the client bucket.
2. Lua script atomically:
   - refills tokens based on elapsed time,
   - consumes one token if available,
   - returns allow/deny + remaining tokens + retry delay.
3. API responds with status:
   - `200 OK` when allowed
   - `429 TOO MANY REQUESTS` when throttled.

Because state is in Redis, multiple app instances enforce the same limit consistently.

## Run locally

### 1) Start Redis

```bash
docker run --name rate-limit-redis -p 6379:6379 redis:7-alpine
```

### 2) Start the app

```bash
cd distributed-rate-limiter
./mvnw spring-boot:run
```

(If wrapper is missing locally, use `mvn spring-boot:run`.)

### 3) Test endpoint

```bash
curl -i -H "X-Client-Id: demo-user" http://localhost:8080/api/resource
```

Repeated calls beyond bucket capacity return `429` with `Retry-After`.

## Configuration

`src/main/resources/application.yml`

- `rate-limiter.default-capacity`: max tokens in bucket
- `rate-limiter.default-refill-tokens`: tokens added each refill period
- `rate-limiter.default-refill-period-seconds`: refill period length

## Key classes

- `DistributedRateLimiterService` – calls Redis Lua script and maps response.
- `RateLimitController` – sample protected endpoint.
- `token_bucket.lua` – atomic token bucket implementation.
