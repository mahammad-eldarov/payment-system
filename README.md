# Payment System

A banking payment system built with Spring Boot for managing customers, cards, current accounts, and payment transactions.

## Tech Stack

- **Java 17**
- **Spring Boot 4.0.2**
- **PostgreSQL**
- **Liquibase** — database migrations
- **Spring Data JPA / Hibernate**
- **Spring Security**
- **Spring Cloud OpenFeign** — external HTTP clients
- **MapStruct** — object mapping
- **ShedLock** — distributed scheduler locks
- **SpringDoc OpenAPI (Swagger)**
- **Gradle**
- **Lombok**

## Features

### Customer Management
- Create, update, and soft-delete customers
- Customer statuses: `ACTIVE`, `SUSPICIOUS`, `BLOCKED`, `CLOSED`
- Multi-language support (Azerbaijani / English)

### Card Management
- Order and manage payment cards
- Card statuses: `ACTIVE`, `BLOCKED`, `EXPIRED`, `CLOSED`
- 16-digit PAN generation, CVV, expiry date
- Balance management with minimum balance enforcement
- Automatic expiration detection via scheduler

### Current Account Management
- Order and manage current accounts
- Account statuses: `ACTIVE`, `BLOCKED`, `SUSPENDED`, `CLOSED`
- 18-digit account number generation
- Balance management with minimum balance enforcement

### Payment Processing
- Card-to-Card, Card-to-Account, Account-to-Card, Account-to-Account transfers
- Idempotency support to prevent duplicate payments
- Payment cooldown / rate limiting
- Payment statuses: `PENDING`, `PROCESSING`, `COMPLETED`, `FAILED`
- Batch payment processing via scheduler

### Transaction Management
- Full transaction history for cards and accounts
- Paginated transaction retrieval
- Transaction statuses: `PENDING`, `COMPLETED`, `FAILED`

### Fraud Detection
- Fraud blacklist management
- Suspicious account detection (max 3 accounts per customer PIN)
- Threshold-based suspicious transaction detection
- Status audit logs for all entity changes

### Scheduling (Daily)
- `00:00` — Card expiration check
- `00:01` — Current account expiration check
- `00:05` — Pending payment processing

### External Integration
- CBAR (Central Bank of Azerbaijan) currency rate client
- Currency conversion support

## Project Structure

```
src/
├── main/
│   ├── java/az/bank/paymentsystem/
│   │   ├── client/          # Feign clients for external APIs
│   │   ├── config/          # Spring configuration classes
│   │   ├── controller/      # REST controllers (v1)
│   │   ├── dto/             # Request / Response DTOs
│   │   ├── entity/          # JPA entities
│   │   ├── enums/           # Enumerations
│   │   ├── exception/       # Custom exceptions
│   │   ├── mapper/          # MapStruct mappers
│   │   ├── repository/      # Spring Data repositories
│   │   ├── scheduler/       # Scheduled tasks
│   │   ├── service/         # Business logic
│   │   └── util/            # Utility / helper classes
│   └── resources/
│       ├── application.yaml
│       ├── db.changelog/    # Liquibase migration scripts
│       ├── message_az.properties
│       └── message_en.properties
└── test/
```

## API Endpoints

| Resource            | Base Path                    |
|---------------------|------------------------------|
| Customers           | `/api/v1/customers`          |
| Cards               | `/api/v1/cards`              |
| Current Accounts    | `/api/v1/accounts`           |
| Payments            | `/api/v1/payments`           |
| Card Orders         | `/api/v1/card-orders`        |
| Account Orders      | `/api/v1/account-orders`     |
| Transactions        | `/api/v1/transactions`       |
| Notifications       | `/api/v1/notifications`      |
| Audit Logs          | `/api/v1/audit-logs`         |

Full API documentation is available at `/swagger-ui.html` after running the application.

## Getting Started

### Prerequisites

- Java 17+
- PostgreSQL
- Gradle

### Configuration

Update `src/main/resources/application.yaml` with your database credentials:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/payment_system
    username: your_username
    password: your_password
```

### Run

```bash
./gradlew bootRun
```

### Build

```bash
./gradlew build
```

## Database

Database schema is managed by **Liquibase** and applied automatically on startup.

Key tables: `customer`, `card`, `current_account`, `card_order`, `current_account_order`, `payment`, `transaction`, `fraud_blacklist`, `notification`, `status_audit_log`, `shedlock`

## License

This project is for educational / demonstration purposes.
