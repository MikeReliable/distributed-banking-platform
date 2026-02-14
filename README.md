## 💳 Disributed Bank Platform

This project presents a distributed banking system built with Spring Boot 3 and microservice architecture.

The system simulates a simplified banking domain including users, accounts, cards and transfers.
Each service is isolated, independently deployable and communicates via REST and Kafka event-driven messaging.

### 🧱 Architecture
The system consists of the following services:
* **api-gateway** – Spring Cloud Gateway (WebFlux)
* **auth-service** – authentication & JWT issuing (RS256)
* **user-service** – user management + Outbox publishing
* **card-service** – card management + Outbox publishing
* **transfer-service** – money transfers + analytics
* **Kafka** – asynchronous communication
* **PostgreSQL** – separate database per service

### 📌 Architectural Principles
* Database per service
* Transactional Outbox pattern
* Idempotent operations (Idempotency-Key)
* Optimistic locking for monetary operations
* Distributed tracing with request propagation
* Native SQL for analytical queries
* Domain-driven custom exceptions
* OpenAPI documentation per service

### 🔐 Key Technical Features
### Authentication & Security
* JWT (RS256) signed tokens
* Spring Security + OAuth2 Resource Server
* Dedicated auth-service
* Stateless authentication

### Idempotency
Critical operations support Idempotency-Key header:
* User creation (user-service)
* Transfers / top-up / withdraw (transfer-service)
Implemented via IdempotentRequest table to guarantee safe retries.

### Transactional Outbox Pattern
Used in:
* user-service
* card-service

Domain events are stored in the same DB transaction and later published to Kafka.

Ensures:
* No lost events
* No dual-write problem
* Eventual consistency between services

### Money Safety
* @Version optimistic locking in Account entity
* Protection against lost updates
* Validation of currency consistency
* Insufficient funds checks
* Same-account transfer protection

### 🔁 Distributed Tracing
Each request contains X-Request-Id.
*  Stored in MDC
*  Propagated via Feign interceptor
*  Persisted inside Outbox events
Provides full traceability across services.

Native SQL Analytics
Transfer-service includes analytical endpoints implemented via native SQL:
*  Account turnover for period
*  Top N transfers by amount
This demonstrates:
*  Complex joins
*  Aggregations
*  Grouping
*  Performance-oriented queries
Swagger (transfer-service):
http://localhost:8083/swagger-ui/index.html

### ❗ Custom Domain Exceptions
Each service uses structured API exceptions:
Example:
* CardNotFoundException
* UserNotFoundException
* InsufficientFundsException
* CurrencyMismatchException
All errors return structured JSON with:
* error code
* HTTP status
* message

### 🧩 Technologies
### Core
* Java 17
* Spring Boot 3.1.6
* Spring Cloud Gateway 4.0.9
* Spring Security + OAuth2
* WebFlux + Reactor Netty
* Spring Data JPA
* Hibernate Core 6.2.13.Final
* Jakarta Persistence 3.1.0
* Hibernate Validator 8.0.1.Final
### Infrastructure
* PostgreSQL 15
* PostgreSQL Driver 42.6.0
* Apache Kafka 2.8.3
* Confluent Platform 7.5.0
* Liquibase
* Docker & Docker Compose
### 🧪 Testing
* JUnit 5
* AssertJ
* Awaitility
* Testcontainers

### ▶ Getting Started
Requirements:
* JDK 17+
* Docker
* Docker Compose
  
Clone repository:
```sh
git clone https://github.com/MikeReliable/bank-rest.git
```
Start infrastructure and services:
```sh
docker-compose up --build
```
### Swagger Endpoints

* Auth Service:

[http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)
* User Service:
  
[http://localhost:8082/swagger-ui/index.html](http://localhost:8082/swagger-ui/index.html)
* Card Service:
  
[http://localhost:8083/swagger-ui/index.html](http://localhost:8083/swagger-ui/index.html)
* Transfer Service:
  
[http://localhost:8084/swagger-ui/index.html](http://localhost:8084/swagger-ui/index.html)
