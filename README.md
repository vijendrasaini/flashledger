# FlashLedger: Distributed High-Throughput Booking & Double-Entry Ledger Engine

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Redis](https://img.shields.io/badge/Redis-8.8-red.svg)](https://redis.io/)
[![Redisson](https://img.shields.io/badge/Redisson-4.7.0-blue.svg)](https://redisson.org/)
[![MySQL](https://img.shields.io/badge/MySQL-9.0-lightblue.svg)](https://www.mysql.com/)
[![Flyway](https://img.shields.io/badge/Flyway-10-crimson.svg)](https://flywaydb.org/)
[![Architecture](https://img.shields.io/badge/Architecture-Concurrency%20%7C%20Fintech%20Ledger-purple.svg)]()

> **FlashLedger** is a production-grade, distributed booking and double-entry financial ledger engine engineered in **Java 21**, **Spring Boot 3.4.5**, **Redis (Redisson 4.7.0)**, and **MySQL 9**. It solves the core concurrency and data integrity challenges of high-traffic flash sales and payment gateways: **zero inventory overselling under high contention**, **strict mathematical balance invariants ($\sum \text{Debits} == \sum \text{Credits}$)** with zero balance drift, and a **distributed idempotency key engine** delivering $\sim$4ms repeat response times.

---

## 1. Architectural Highlights & Core Pillars

| Distributed Pattern | Problem Solved | FlashLedger Implementation |
| :--- | :--- | :--- |
| **Distributed Fair Locking (Redisson)** | Concurrent threads reading inventory simultaneously read stale quantities, causing overselling and database connection pool starvation. | Uses Redisson Fair Locks (`RReadWriteLock` / `RLock`) with FIFO queueing. Implements the **Lock Acquisition Strategy Pattern** (`FailFastLockStrategy` vs. `BoundedWaitLockStrategy`) backed by Redisson's watchdog auto-renewal. |
| **Double-Entry Financial Ledger** | Direct `UPDATE accounts SET balance = balance - amount` leaves no audit trail, is non-auditable, lossy, and causes ledger drift. | Maintains immutable transactional journals (`accounts`, `ledger_transactions`, `ledger_entries`). Enforces strict mathematical equality ($\sum \text{DEBIT} == \sum \text{CREDIT}$) inside atomic `@Transactional` boundaries. Balances are derived dynamically (`SUM(CREDIT) - SUM(DEBIT)`). |
| **Distributed Idempotency Engine** | Network timeouts cause clients or frontends to resend requests, triggering duplicate orders or double deductions. | Intercepts HTTP `Idempotency-Key` headers. Caches serialized `OrderDetailsDTO` in Redis `RBucket` with a 24-hour TTL, returning repeat responses in $\sim$4ms while serializing concurrent double-clicks. |
| **Fail-Fast Database Shielding** | Surge traffic exhausts HikariCP database connections, causing cascading latency spikes and thread exhaustion. | Under heavy contention, `FailFastLockStrategy` immediately rejects excess requests before database transactions begin, shielding MySQL from thread pileups. |
| **Zero Balance Drift & Reconciliation** | Rounding errors, race conditions, or unhandled exceptions corrupt account balances over time. | Account balances are never stored as single mutable scalar columns. Every balance query calculates the delta of immutable ledger entries, guaranteeing zero drift by mathematical definition. |

---

## 2. System Architecture & Component Layout

```text
                                [ HTTP Client ]
                                       |
                                       v (POST /orders with Idempotency-Key)
                        +------------------------------+
                        |    OrderController (8080)    |
                        +--------------+---------------+
                                       |
            +--------------------------+--------------------------+
            |                                                     |
            v                                                     v
+------------------------------+                       +------------------------------+
|  Distributed Idempotency     |                       | Distributed Lock Strategy    |
|  - Check Redis RBucket cache |                       | - FailFast: immediate reject |
|  - If hit: return in ~4ms    |                       | - BoundedWait: FIFO queue    |
+------------------------------+                       +--------------+---------------+
                                                                      | (Lock Acquired)
                                                                      v
                                                       +------------------------------+
                                                       |      Order & Stock Service   |
                                                       |  - Validate stock & price    |
                                                       |  - Deduct inventory reserve  |
                                                       +--------------+---------------+
                                                                      | (Atomic Transaction)
                                                                      v
                                                       +------------------------------+
                                                       |  Double-Entry Ledger Engine  |
                                                       |  - DEBIT: Buyer account      |
                                                       |  - CREDIT: Revenue/Escrow    |
                                                       |  - Assert: SUM(D) == SUM(C)  |
                                                       +--------------+---------------+
                                                                      |
                                  +-----------------------------------+-----------------------------------+
                                  |                                                                       |
                                  v                                                                       v
                   +------------------------------+                                        +------------------------------+
                   |           MySQL 9            |                                        |           Redis 8            |
                   |  - orders & order_items      |                                        |  - Redisson Fair Locks       |
                   |  - inventory & products      |                                        |  - Idempotency Cache (24h)   |
                   |  - ledger_transactions       |                                        |  - Redisson Watchdog Lease   |
                   |  - ledger_entries & accounts |                                        +------------------------------+
                   +------------------------------+
```

---

## 3. Core Architectural Lifecycles & Sequence Flows

### 3.1 Flash-Sale Booking with Distributed Fair Locking

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant Controller as OrderController
    participant Idemp as IdempotencyService
    participant Redis as Redis (Redisson)
    participant LockStrategy as LockStrategy (BoundedWait)
    participant OrderSvc as OrderService
    participant Ledger as LedgerService
    participant MySQL as MySQL 9

    Client->>Controller: POST /orders (Idempotency-Key: abc-123)
    Controller->>Idemp: checkCachedResponse("abc-123")
    Idemp->>Redis: GET order:idempotency:abc-123
    Redis-->>Idemp: null (Cache Miss)

    Controller->>LockStrategy: executeWithLock(productId, task)
    LockStrategy->>Redis: Acquire Redisson FairLock (wait=5s, lease=3s)
    activate Redis
    Note over Redis: Watchdog starts auto-renewing lease
    Redis-->>LockStrategy: Lock Acquired (FIFO Order)

    LockStrategy->>OrderSvc: processOrder()
    activate OrderSvc
    OrderSvc->>MySQL: BEGIN TX
    OrderSvc->>MySQL: Check available stock (units > 0)
    OrderSvc->>MySQL: Decrement stock & insert Order

    OrderSvc->>Ledger: recordTransaction(Buyer, SystemRevenue, Amount)
    Ledger->>MySQL: Insert ledger_transactions
    Ledger->>MySQL: Insert Entry 1 (DEBIT Buyer, -Amount)
    Ledger->>MySQL: Insert Entry 2 (CREDIT Revenue, +Amount)
    Note over Ledger,MySQL: Invariant Check: SUM(Debits) == SUM(Credits)

    OrderSvc->>MySQL: COMMIT TX
    deactivate OrderSvc

    OrderSvc->>Redis: Cache OrderDetailsDTO in RBucket (TTL 24h)
    LockStrategy->>Redis: Release Redisson FairLock
    deactivate Redis

    Controller-->>Client: 201 Created (OrderDetailsDTO)
```

---

### 3.2 Distributed Idempotency: Sub-5ms Repeat Response

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant Controller as OrderController
    participant Idemp as IdempotencyService
    participant Redis as Redis (Redisson)
    participant MySQL as MySQL 9

    Note over Client,Controller: Network drop causes client retry with same Idempotency-Key
    Client->>Controller: POST /orders (Idempotency-Key: abc-123)
    Controller->>Idemp: checkCachedResponse("abc-123")
    activate Idemp
    Idemp->>Redis: GET order:idempotency:abc-123
    Redis-->>Idemp: Cached OrderDetailsDTO
    deactivate Idemp

    Note over Controller,MySQL: Zero Database Queries! Zero Locks! Zero Overhead!
    Controller-->>Client: 200 OK (Cached OrderDetailsDTO, ~4ms)
```

---

### 3.3 Double-Entry Accounting Ledger Mechanics

```mermaid
graph TD
    subgraph Atomic_Transaction [Atomic Database Transaction]
        Tx[Ledger Transaction ID: tx-98712]
        Tx -->|Creates| Entry1["Entry 1: Account (Buyer Wallet)<br/>Direction: DEBIT<br/>Amount: $150.00"]
        Tx -->|Creates| Entry2["Entry 2: Account (System Escrow)<br/>Direction: CREDIT<br/>Amount: $150.00"]
    end

    subgraph Mathematical_Invariant [Mathematical Invariant]
        SumDebit["Total Debits = $150.00"]
        SumCredit["Total Credits = $150.00"]
        Validation{"Debits == Credits?"}
        SumDebit --> Validation
        SumCredit --> Validation
        Validation -->|TRUE| Commit[COMMIT TX]
        Validation -->|FALSE| Rollback[ROLLBACK TX]
    end
```

---

## 4. Multi-Module & Directory Topography

```text
flashledger/
├── docker-compose.yml              # MySQL 9.0 (Port 3306) & Redis 8.8 (Port 6379)
├── pom.xml                         # Java 21, Spring Boot 3.4.5, Redisson 4.7.0, Flyway 10
└── src/
    ├── main/
    │   ├── java/com/flashledger/flashledgerengine/
    │   │   ├── bootstrap/          # SystemAccountSeeder (Idempotent revenue account initialization)
    │   │   ├── config/             # RedissonConfig, JacksonConfig
    │   │   ├── controller/         # OrderController (Idempotency-Key handling & REST API)
    │   │   ├── dto/                # CreateOrderRequest, OrderDetailsDTO, ApiResponse
    │   │   ├── entity/             # Account, LedgerTransaction, LedgerEntry, Order, Product, Inventory
    │   │   ├── repository/         # AccountRepository, LedgerEntryRepository, OrderRepository...
    │   │   ├── service/            # OrderService, LedgerService, IdempotencyService
    │   │   └── strategy/           # LockStrategy, FailFastLockStrategy, BoundedWaitLockStrategy
    │   └── resources/
    │       ├── application.yml     # Database, Redis & Redisson properties
    │       └── db/migration/       # Flyway V1-V8 migration scripts
    └── test/                       # 11/11 automated integration tests
```

---

## 5. Technology Stack

- **Runtime & Language**: Java 21 LTS
- **Framework**: Spring Boot 3.4.5, Spring Data JPA, Spring Validation
- **Distributed Locking & Caching**: Redisson 4.7.0, Redis 8.8 Alpine
- **Database**: MySQL 9.0
- **Database Migrations**: Flyway 10 (V1 through V8 scripts)
- **Connection Pool**: HikariCP (leak detection and connection timeouts)
- **Testing**: JUnit 5, MockMvc, Spring Boot Test (`@SpringBootTest`)
- **Containers**: Docker Compose (MySQL 9 on 3306, Redis on 6379)

---

## 6. Automated Test Suite Matrix

FlashLedger is verified by an automated integration test suite running against real Redis and MySQL infrastructure:

| Test Class | Focus & Guarantee Tested | Result |
| :--- | :--- | :--- |
| `OrderServiceOverSellingTest` | **Zero Overselling**: 10 concurrent threads compete for scarce inventory; verifies exact stock count and zero oversold units. | Passed |
| `OrderServiceBoundedWaitTest` | **FIFO Fair Queueing**: Bounded wait strategy queues contending threads in fair order under watchdog renewal. | Passed |
| `OrderServiceFailFastTest` | **Database Protection**: Immediate rejection of excess requests under contention before hitting database transactions. | Passed |
| `LedgerServiceTest` | **Mathematical Invariant**: Verifies $\sum \text{Debits} == \sum \text{Credits}$ and rollback on unbalanced transactions. | Passed |
| `ServiceOrderIdempotencyTest` (Sequential) | **Sub-5ms Cache Replay**: Repeat request with identical `Idempotency-Key` returns cached order details in $\sim$4ms. | Passed |
| `ServiceOrderIdempotencyTest` (Concurrent) | **Double-Click Serialization**: Concurrent identical requests result in exactly 1 database write and 1 cached response. | Passed |
| `OrderServiceWithLedgerTest` | **Atomic Cross-Domain Coupling**: Verifies order placement and double-entry ledger entries commit atomically. | Passed |
| `OrderControllerIntegrationTest` | **Full HTTP Lifecycle**: MockMvc verification of `POST /orders`, HTTP status 201, and header extraction. | Passed |
| `SystemAccountSeederTest` | **Startup Idempotency**: Verified system revenue and escrow accounts initialize idempotently without duplicates. | Passed |

To run the complete test suite:
```bash
./mvnw clean test
```

---

## 7. Local Infrastructure Setup

### Prerequisites
- Java 21 LTS installed
- Docker and Docker Compose running

### Step 1: Start Infrastructure Containers
```bash
# Start MySQL 9 and Redis 8
docker compose up -d
```

### Step 2: Run Database Migrations & Start Engine
```bash
./mvnw spring-boot:run
```
Flyway will automatically execute migrations `V1__create_users_table.sql` through `V8__create_ledger_entries_table.sql`.

### Step 3: Place a High-Concurrency Order (with Idempotency Key)
```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: 9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d" \
  -d '{
    "userId": 1,
    "productId": 101,
    "quantity": 1
  }'
```

Replaying the exact same `curl` command with the same `Idempotency-Key` will immediately return the cached order response in $\sim$4ms without touching MySQL.