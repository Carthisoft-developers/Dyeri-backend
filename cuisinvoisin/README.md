# Cuisin'Voisin — Backend

> Home-cooked meal ordering platform connecting clients, home cooks, and delivery drivers in Tunisia.
> **Spring Boot 3.3 · Java 21 · Hexagonal Architecture · Single deployable JAR**

---

## Architecture

```
com.cuisinvoisin/
├── domain/          ← Core hex ring (pure Java — no Spring)
│   ├── entities/    ← JPA entities (24 classes, single-table inheritance)
│   ├── repositories/← Outbound ports — Spring Data JPA interfaces (19)
│   ├── services/    ← Inbound ports — plain Java interfaces (15)
│   └── exceptions/  ← Typed domain exceptions (4)
├── application/     ← Orchestration ring
│   ├── services/    ← Use-case implementations @Service (15)
│   ├── bean/        ← Java 21 records: request (15) + response (21)
│   └── mappers/     ← MapStruct mappers (8)
├── infrastructure/  ← Outbound adapters
│   ├── config/      ← Spring @Configuration (7)
│   ├── security/    ← JWT + filter + WS interceptor (4)
│   ├── cache/       ← Redis adapters (2)
│   ├── persistence/ ← JPA Specifications (1)
│   └── storage/     ← File storage adapter (1)
├── interfaces/      ← Inbound adapters
│   ├── rest/        ← REST controllers (13)
│   ├── websocket/   ← STOMP handler (1)
│   └── advice/      ← GlobalExceptionHandler (1)
└── shared/          ← Enums + utils
```

---

## Tech Stack

| Concern | Technology |
|---|---|
| Language | Java 21 (records, switch expressions, pattern matching) |
| Framework | Spring Boot 3.3 |
| Persistence | PostgreSQL 16 + Spring Data JPA + Hibernate 6 |
| Migrations | Flyway |
| Security | Spring Security 6 — stateless JWT (JJWT 0.12) |
| Cache / Sessions | Redis 7 (Lettuce) |
| Real-time | Spring WebSocket (STOMP) |
| Mapping | MapStruct 1.6 |
| Rate Limiting | Bucket4j |
| API Docs | SpringDoc OpenAPI 3 → `/swagger-ui.html` |
| Tests | JUnit 5 + Mockito + MockMvc + Testcontainers |

---

## Prerequisites

| Tool | Version |
|---|---|
| Java | 21+ |
| Maven | 3.9+ |
| PostgreSQL | 16 |
| Redis | 7 |
| Docker (optional) | for Testcontainers |

---

## Quick Start

### 1. Start infrastructure

```bash
docker run -d --name cv-pg \
  -e POSTGRES_DB=cuisinvoisin \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 postgres:16-alpine

docker run -d --name cv-redis \
  -p 6379:6379 redis:7-alpine
```

### 2. Set environment variables

```bash
export JWT_SECRET="your-super-secret-key-minimum-32-characters"
export DB_USER=postgres
export DB_PASSWORD=postgres
```

### 3. Run (dev profile)

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Flyway runs automatically on startup — schema + seed data applied.

### 4. API documentation

```
http://localhost:8080/swagger-ui.html
```

---

## Demo Accounts

Seeded by `V3__seed_demo_users.sql`. Password for all: **`password`**

| Email | Role |
|---|---|
| `client@test.com` | CLIENT |
| `cook@test.com` | COOK |
| `delivery@test.com` | DELIVERY |

---

## Key API Endpoints

### Authentication
```
POST /api/v1/auth/register
POST /api/v1/auth/login
POST /api/v1/auth/refresh
POST /api/v1/auth/logout
```

### Catalogue (public)
```
GET /api/v1/categories
GET /api/v1/dishes?cookId=&categoryId=&minPrice=&maxPrice=&available=&query=
GET /api/v1/dishes/{id}
GET /api/v1/cooks?lat=&lng=&radius=
GET /api/v1/cooks/{id}
GET /api/v1/search?q=&type=all|dish|cook
```

### Cart & Orders
```
GET    /api/v1/cart
POST   /api/v1/cart/items
PATCH  /api/v1/cart/items/{id}
DELETE /api/v1/cart/items/{id}
DELETE /api/v1/cart

POST   /api/v1/orders
GET    /api/v1/orders
GET    /api/v1/orders/{id}
PATCH  /api/v1/orders/{id}/status
DELETE /api/v1/orders/{id}/cancel
```

### Delivery (DRIVER role)
```
GET  /api/v1/delivery/orders
POST /api/v1/delivery/orders/{id}/accept
POST /api/v1/delivery/orders/{id}/location
POST /api/v1/delivery/orders/{id}/complete
GET  /api/v1/delivery/earnings
```

---

## WebSocket (STOMP)

Connect to: `ws://localhost:8080/ws`
Include JWT in the `Authorization` header on CONNECT.

| Channel | Direction | Purpose |
|---|---|---|
| `/topic/orders/{id}/status` | Server → Client | Real-time order timeline |
| `/queue/orders/new` | Server → Cook | New order notification |
| `/topic/orders/available` | Server → Drivers | Available delivery orders |

---

## Order State Machine

```
PENDING ──(COOK)──► ACCEPTED ──(COOK)──► PREPARING ──(COOK)──► READY
   │                                                              │
   └──(any)──► CANCELLED                              (DRIVER)──►ASSIGNED
                                                                  │
                                                        (DRIVER)──►PICKED_UP
                                                                  │
                                                    (DRIVER)──►OUT_FOR_DELIVERY
                                                                  │
                                                        (DRIVER)──►DELIVERED
```

---

## Running Tests

```bash
# Unit tests only (no Docker needed)
./mvnw test -pl . -Dtest="JwtUtilTest,OrderServiceImplTest,CartServiceImplTest,DishSpecificationTest"

# All tests (requires Docker for Testcontainers)
./mvnw verify
```

---

## Production Deployment

```bash
./mvnw package -DskipTests -Pprod
java -jar target/cuisin-voisin-1.0.0-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --DB_URL=jdbc:postgresql://... \
  --DB_USER=... \
  --DB_PASSWORD=... \
  --REDIS_HOST=... \
  --REDIS_PASSWORD=... \
  --JWT_SECRET=...
```

---

## Project Stats

| Category | Count |
|---|---|
| Java source files | 162 |
| Domain entities | 24 |
| Repository interfaces | 19 |
| Service interfaces | 15 |
| Service implementations | 15 |
| REST controllers | 13 |
| MapStruct mappers | 8 |
| Flyway migrations | 3 |
| Test classes | 5 |
