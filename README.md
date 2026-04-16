# OrderFlow - E-Commerce Order Management System

A full-stack microservices-based e-commerce platform demonstrating cloud-ready architecture, event-driven order processing, and modern frontend development.

## Architecture

```
  ┌──────────────────┐
  │  Angular Frontend │ (port 4200)
  │  Product Catalog  │
  │  Cart + Checkout  │
  │  Order Tracking   │
  └────────┬─────────┘
           │
           ▼
  ┌─────────────────┐
  │   API Gateway    │ (port 8080) — JWT Auth + Routing
  └────────┬────────┘
           │
  ┌────────┼────────────────┬──────────────┐
  ▼        ▼                ▼              ▼
┌────────┐ ┌─────────┐ ┌───────────┐ ┌──────────────┐
│ Order  │ │ Payment │ │ Inventory │ │ Notification │
│Service │ │ Service │ │  Service  │ │   Service    │
│(8081)  │ │ (8082)  │ │  (8083)   │ │   (8084)     │
└───┬────┘ └────┬────┘ └─────┬─────┘ └──────┬───────┘
    │           │            │               │
    ▼           ▼            ▼               ▼
[PostgreSQL] [PostgreSQL] [PostgreSQL]  [PostgreSQL]
[Redis]                   [S3/LocalStack]
    │           │            │               ▲
    └───────► [Apache Kafka] ◄───────────────┘
```

## Tech Stack

| Technology | Purpose |
|---|---|
| **Spring Boot 3.2** | Microservice framework (Java 17) |
| **Spring Cloud Gateway** | API Gateway with reactive routing |
| **Spring Security + JWT** | Authentication & authorization |
| **Apache Kafka** | Event-driven order processing pipeline |
| **Redis** | Distributed caching (products, order status) |
| **PostgreSQL** | Relational database (per-service) |
| **AWS S3 (LocalStack)** | Product image storage |
| **Angular 17** | Frontend SPA |
| **Nginx** | Frontend serving + API reverse proxy |
| **Docker Compose** | Container orchestration |
| **Maven** | Multi-module build system |

## Kafka Event Flow

```
Customer places order
    │
    ▼
Order Service → ORDER_PLACED
    │
    ▼
Payment Service → PAYMENT_COMPLETED / PAYMENT_FAILED
    │
    ▼ (on success)
Inventory Service → STOCK_RESERVED / STOCK_UNAVAILABLE
    │
    ▼ (on reserved)
Order Service → CONFIRMED → SHIPPED → DELIVERED
    │
Notification Service listens to ALL events → sends notifications
```

## Quick Start

### Prerequisites
- Docker & Docker Compose
- Java 17+ (for local development)
- Node.js 20+ (for frontend development)
- Maven 3.9+

### Run with Docker Compose

```bash
docker compose up --build
```

This starts all services, databases, Kafka, Redis, LocalStack, and the frontend.

**Access the application:**
- Frontend: http://localhost:4200
- API Gateway: http://localhost:8080

**Default admin credentials:**
- Email: `admin@orderflow.com`
- Password: `admin123`

### Local Development

1. Start infrastructure:
```bash
docker compose up postgres-orders postgres-payments postgres-inventory postgres-notifications redis zookeeper kafka localstack
```

2. Build and run backend:
```bash
mvn clean install -DskipTests
# Run each service individually or use your IDE
```

3. Run frontend:
```bash
cd frontend
npm install
ng serve
```

## API Endpoints

### Authentication
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login and get JWT token |
| GET | `/api/users/me` | Get current user profile |

### Products
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/products` | List all products |
| GET | `/api/products/{id}` | Get product details |
| GET | `/api/products/category/{cat}` | Filter by category |
| GET | `/api/products/search?q=` | Search products |
| POST | `/api/products` | Create product (admin) |
| PUT | `/api/products/{id}` | Update product (admin) |
| POST | `/api/products/{id}/image` | Upload product image |

### Orders
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/orders` | Place new order |
| GET | `/api/orders/{id}` | Get order details |
| GET | `/api/orders/user/{userId}` | Get user's orders |
| PUT | `/api/orders/{id}/status` | Update order status (admin) |
| DELETE | `/api/orders/{id}` | Cancel order |

### Payments
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/payments/order/{orderId}` | Get payment by order |
| GET | `/api/payments/user/{userId}` | Get user's payments |

### Notifications
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/notifications/user/{userId}` | Get user notifications |
| GET | `/api/notifications/order/{orderId}` | Get order notifications |

### Inventory
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/inventory/{productId}` | Get stock info |
| PUT | `/api/inventory/{productId}/stock` | Update stock (admin) |

## Project Structure

```
order-flow-service/
├── common/                  # Shared DTOs, events, exceptions, JWT utility
├── api-gateway/             # Spring Cloud Gateway with JWT filter
├── order-service/           # User auth + order management
├── payment-service/         # Payment processing (simulated)
├── inventory-service/       # Product catalog + S3 image storage
├── notification-service/    # Event-driven email notifications
├── frontend/                # Angular 17 SPA
├── localstack-init/         # S3 bucket initialization script
├── docker-compose.yml       # Full stack orchestration
└── pom.xml                  # Parent Maven POM
```

## Key Design Decisions

1. **Auth inside Order Service** — Avoids a 5th microservice. Order service owns the user table since orders are user-centric.

2. **Event choreography** — Each service reacts independently to Kafka events. No central coordinator. More resilient and demonstrates true event-driven patterns.

3. **LocalStack for S3** — No AWS account needed. Uses AWS SDK v2 with configurable endpoint. Switching to real AWS is a one-line config change.

4. **Separate consumer groups** — Every service gets every event it subscribes to, independently processed.

5. **Simulated payments** — Amounts under $10,000 always succeed. Shows the pattern without needing Stripe/PayPal.

6. **Seed data on startup** — 20 products across 6 categories + admin user. Demo-ready on first `docker compose up`.

7. **Client-side cart** — Stored in localStorage via Angular CartService. Simpler than server-side for a portfolio demo.

## Running Tests

```bash
mvn test
```

Unit tests use H2 in-memory database with Kafka/Redis/S3 disabled via test profiles.
