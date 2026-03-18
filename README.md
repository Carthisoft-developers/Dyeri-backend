# Dyeri Platform — Reactive Microservices

> Home-cooked meal ordering platform connecting clients, home cooks, and delivery drivers in Tunisia.
> **Spring Boot 3.3 · Java 21 · WebFlux · R2DBC · Kafka · Keycloak · Hexagonal Architecture**

---

## Architecture Overview

```
                         ┌─────────────────────────────────────┐
                         │           API Gateway :8080          │
                         │  Spring Cloud Gateway + Keycloak JWT │
                         │  RedisRateLimiter · Resilience4j CB  │
                         └──────────────┬──────────────────────┘
                    LB (Eureka)         │        Token Relay
          ┌─────────────┬───────────────┼──────────────┐
          │             │               │              │
   ┌──────▼──────┐ ┌────▼────┐  ┌──────▼──────┐ ┌────▼────────┐
   │ core-service│ │         │  │notification │ │  payment    │
   │    :8081    │ │ Keycloak│  │  service    │ │  service    │
   │  WebFlux    │ │  :8180  │  │    :8082    │ │   :8083     │
   │  R2DBC PG   │ │         │  │ Kafka↓ R2DBC│ │ Kafka↓ R2DBC│
   └──────┬──────┘ └─────────┘  └─────────────┘ └─────────────┘
          │ Kafka Events
          │  orders.placed ──────────────────────────► payment-service
          │  orders.status-changed ──────────────────► notification-service
          │  notifications.send ─────────────────────► notification-service
          │                       payments.confirmed ◄─ payment-service
          │
   ┌──────▼──────────────────────────────────────────────────────┐
   │           Infrastructure                                     │
   │  PostgreSQL :5432  ·  Redis :6379  ·  Kafka :9092           │
   │  Eureka :8761  ·  Config Server :8888  ·  MailHog :1025      │
   └─────────────────────────────────────────────────────────────┘
```

---

## Tech Stack

| Concern | Technology |
|---|---|
| Language | Java 21 (records, switch expressions, virtual threads) |
| Framework | Spring Boot 3.3 + Spring WebFlux |
| Persistence | PostgreSQL 16 + R2DBC (non-blocking) |
| Migrations | Flyway (JDBC driver only — schema bootstrap) |
| Caching | Redis 7 (ReactiveRedisTemplate, cache-aside pattern) |
| Messaging | Apache Kafka 7.6 + reactor-kafka (KafkaReceiver) |
| Identity | Keycloak 24 (realm: dyeri, JWT RS256) |
| API Gateway | Spring Cloud Gateway + Resilience4j |
| Load Balancer | Spring Cloud LoadBalancer (Eureka-backed) |
| Circuit Breaker | Resilience4j (CB + Retry + Bulkhead) |
| Rate Limiting | Spring Cloud Gateway RedisRateLimiter (Layer 1) + Bucket4j (Layer 2) |
| Service Registry | Netflix Eureka |
| Config Server | Spring Cloud Config (native) |
| Mapping | MapStruct 1.6 |
| API Docs | SpringDoc OpenAPI 3 (WebFlux variant) |
| Tests | JUnit 5 + Mockito + StepVerifier + WebTestClient |
| Observability | Actuator + Micrometer + Prometheus |

---

## Services & Ports

| Service | Port | Description |
|---|---|---|
| `api-gateway` | 8080 | Entry point — routing, JWT validation, rate limiting, CB |
| `core-service` | 8081 | Users, Cooks, Catalogue, Cart, Orders, Delivery, Reviews |
| `notification-service` | 8082 | Kafka consumer → persist + email notifications |
| `payment-service` | 8083 | Kafka consumer/producer → payment processing + payouts |
| `discovery-server` | 8761 | Eureka service registry |
| `config-server` | 8888 | Spring Cloud Config |
| Keycloak | 8180 | SSO / JWT issuer |
| Kafka UI | 9080 | Kafka topic browser |
| MailHog | 8025 | Dev email catcher (SMTP: 1025) |

---

## Kafka Topics

| Topic | Producer | Consumer(s) |
|---|---|---|
| `dyeri.orders.placed` | core-service | payment-service |
| `dyeri.orders.status-changed` | core-service | notification-service |
| `dyeri.payments.confirmed` | payment-service | notification-service |
| `dyeri.payments.failed` | payment-service | notification-service |
| `dyeri.notifications.send` | core-service | notification-service |

All consumers use **reactor-kafka KafkaReceiver** (non-blocking).
All events have **Redis idempotency checks** (TTL 24h/48h) to prevent duplicate processing.

---

## Order State Machine

```
PENDING ──(COOK)──► ACCEPTED ──(COOK)──► PREPARING ──(COOK)──► READY
   │                                                              │
   └──(any)──► CANCELLED                              (DRIVER)──► ASSIGNED
                                                                   │
                                                         (DRIVER)──► PICKED_UP
                                                                   │
                                                     (DRIVER)──► OUT_FOR_DELIVERY
                                                                   │
                                                         (DRIVER)──► DELIVERED
```

State transitions enforced in `OrderServiceImpl.isTransitionAllowed()`.
Each transition publishes `OrderStatusChangedEvent` → Kafka → notification-service.

---

## Redis Cache Keys

| Key | TTL | Content |
|---|---|---|
| `dish:{dishId}` | 10 min | `DishResponse` |
| `cook:{cookId}` | 10 min | `CookResponse` |
| `cart:{clientId}` | 2 hours | `CartResponse` |
| `order:{orderId}` | 5 min | `OrderResponse` |
| `categories:all` | 1 hour | `List<CategoryResponse>` |
| `search:{q}:{type}:{page}` | 3 min | `SearchResultResponse` |
| `notif:processed:{eventId}` | 24 hours | Idempotency key |
| `payment:processed:{eventId}` | 48 hours | Idempotency key |

---

## Rate Limiting

**Layer 1 — API Gateway (RedisRateLimiter)**

| Route | Rate | Burst | Key |
|---|---|---|---|
| `/api/v1/auth/**` | 5 req/s | 10 | IP |
| All other routes | 50 req/s | 100 | User (JWT sub) |

**Layer 2 — Core Service (Bucket4j)**

| Endpoint | Rate | Key |
|---|---|---|
| Auth endpoints | 10 req/s | IP |
| Authenticated | 100 req/min | userId |

Returns **HTTP 429** with `Retry-After` header on exceeded limits.

---

## Circuit Breaker Config

| Service | Window | Failure Threshold | Open Duration |
|---|---|---|---|
| core-service | 10 calls | 50% | 10s |
| notification-service | 10 calls | 60% | 15s |
| payment-service | 10 calls | **40%** | 20s |

Fallback responses:
- `core-service` → **503** `{"error": "Service temporarily unavailable"}`
- `notification-service` → **202** `{"message": "Notification queued"}`
- `payment-service` → **503** `{"error": "Payment service unavailable, retry later"}`

---

## Quick Start

### Prerequisites
- Java 21+ · Maven 3.9+ · Docker & Docker Compose

### 1. Start with Docker Compose (recommended)

```bash
cd dyeri-platform
docker compose up -d
```

Wait for all services to be healthy (~60s), then:

```bash
# API
curl http://localhost:8080/api/v1/categories

# Swagger UI (core-service)
open http://localhost:8081/swagger-ui.html

# Kafka topics browser
open http://localhost:9080

# Keycloak admin
open http://localhost:8180  # admin / admin

# Email (MailHog)
open http://localhost:8025
```

### 2. Run services locally (dev profile)

```bash
# Terminal 1 — Eureka
cd discovery-server && mvn spring-boot:run

# Terminal 2 — Core service
cd core-service && mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Terminal 3 — Notification service
cd notification-service && mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Terminal 4 — Payment service
cd payment-service && mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Terminal 5 — API Gateway
cd api-gateway && mvn spring-boot:run
```

---

## Authentication Flow

```
1. Client → POST http://localhost:8180/realms/dyeri/protocol/openid-connect/token
   Body: grant_type=password, client_id=swagger-ui, username=client@test.com, password=password

2. Keycloak → { access_token, refresh_token, expires_in: 900 }

3. Client → GET http://localhost:8080/api/v1/cart
   Header: Authorization: Bearer <access_token>

4. Gateway → validates JWT against Keycloak JWKS → routes to core-service
             adds TokenRelay header (forwards JWT downstream)

5. core-service → extracts userId from JWT "sub" claim
                  extracts role from JWT "realm_access.roles"
```

**Demo accounts** (password: `password` for all):

| Email | Role |
|---|---|
| `client@test.com` | CLIENT |
| `cook@test.com` | COOK |
| `delivery@test.com` | DELIVERY |

---

## Running Tests

```bash
# Unit tests (no Docker needed)
mvn test -pl core-service -Dtest="ReactiveOrderServiceTest,ReactiveCatalogueServiceTest"

# All tests
mvn verify

# Single service
cd core-service && mvn test
```

---

## Production Build

```bash
# Build all JARs
mvn clean package -DskipTests

# Or build Docker images
docker compose build

# Deploy
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

---

## Project Structure

```
dyeri-platform/
├── pom.xml                    ← Parent BOM
├── shared-events/             ← Kafka event records (shared JAR)
├── discovery-server/          ← Eureka :8761
├── config-server/             ← Spring Cloud Config :8888
├── core-service/              ← Main business logic :8081
│   └── src/main/java/com/dyeri/core/
│       ├── domain/            ← Entities, Repositories (R2DBC), Service interfaces
│       ├── application/       ← Service implementations, DTOs, Mappers, Kafka events
│       ├── infrastructure/    ← Redis cache, Kafka producer, Keycloak client, Security
│       └── interfaces/        ← WebFlux handlers, RouterFunctions, Error handler
├── notification-service/      ← Kafka consumers + email :8082
├── payment-service/           ← Kafka consumers/producers + payouts :8083
├── api-gateway/               ← Gateway routes + CB + rate limiting :8080
├── keycloak/realm-export.json ← Keycloak realm definition
└── docker-compose.yml         ← Full dev environment
```

---

## Key Design Decisions

1. **No .block() anywhere** — every operation returns `Mono<T>` or `Flux<T>`
2. **R2DBC instead of JPA** — entities have no `@OneToMany`; joins done via `DatabaseClient`
3. **Kafka idempotency** — Redis keys checked before processing every consumed event
4. **Cache-aside** — `switchIfEmpty(DB query → cache population)` in all service read paths
5. **Keycloak as SSO** — no custom JWT generation; `AuthController` removed; roles from `realm_access`
6. **Two-layer rate limiting** — Gateway (Redis token bucket) + Core service (Bucket4j)
7. **Circuit breakers at gateway** — payment-service has stricter threshold (40%) than others
