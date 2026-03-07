## 💳 Distributed Bank Platform

This project presents a distributed banking system built with Spring Boot 3 and microservice architecture.

The system simulates a simplified banking domain including users, accounts, cards and transfers.
Each service is isolated, independently deployable and communicates via REST and Kafka event-driven messaging.

### 🧱 Architecture
The system consists of the following services:
* **api-gateway** – Spring Cloud Gateway (WebFlux), single entry point for all clients
* **auth-service** – authentication & JWT issuing (RS256), user registration, publishes `USER_REGISTERED` events
* **user-service** – user profile management, consumes `USER_REGISTERED` to create profiles, publishes `USER_CREATED` and `USER_BLOCKED` events via Outbox
* **card-service** – card management, consumes `USER_CREATED` to create default cards, publishes `CARD_CREATED` events via Outbox
* **transfer-service** – money transfers, consumes `CARD_CREATED` to set up accounts, supports idempotent operations and optimistic locking
* **Kafka** – asynchronous communication (topics: `registered-events`, `created-events`, `blocked-events`, `card-events`)
* **PostgreSQL** – separate database per service

### 📌 Architectural Principles
* Database per service
* Transactional Outbox pattern for reliable event publishing
* Idempotent operations (Idempotency-Key) for registration and financial transactions
* Optimistic locking for monetary operations
* Distributed tracing with request propagation
* Native SQL for analytical queries
* Domain-driven custom exceptions
* OpenAPI documentation per service, aggregated in Gateway

### 🔐 Key Technical Features
### Authentication & Security
* JWT (RS256) signed tokens,  key pair generated in auth‑service and exposed via JWKS endpoint
* Spring Security + OAuth2 Resource Server in each service
* Stateless authentication, tokens validated against auth‑service’s JWKS
* Registration flow: auth‑service stores credentials and publishes `USER_REGISTERED` event

### User Registration & Profile Creation (Event‑Driven)
1. Client calls `POST /auth/register` with username, email, password
2. Auth‑service saves credentials and publishes `USER_REGISTERED` event to Kafka
3. User‑service consumes the event, creates a user profile (with the same UUID), and publishes `USER_CREATED`
4. Card‑service consumes `USER_CREATED` and creates a default debit card for the user
5. Transfer‑service, upon receiving `CARD_CREATED`, sets up a default account for the card

### User Blocking
1. Client calls `POST /users/{id}/block` (soft block) in user‑service.
2. User‑service marks the user as blocked, publishes `USER_BLOCKED` event.
3. Auth‑service consumes the event and blocks the corresponding login credentials

### Idempotency
Critical operations support `Idempotency-Key` header:
* User registration (auth-service)
* Transfers / top-up / withdraw (transfer-service)

Implemented via `IdempotentRequest` table to guarantee safe retries

### Transactional Outbox Pattern
Used in **user‑service**, **card‑service**, and **auth‑service**

Domain events are stored in the same DB transaction and later published to Kafka

Ensures:
* No lost events
* No dual-write problem
* Reliable event delivery between services

### Money Safety
* `@Version` optimistic locking in `Account` entity prevents lost updates
* Validation of currency consistency, insufficient funds, and same‑account transfers
* All money movements are atomic and consistent

### 🔁 Distributed Tracing
Each request contains `X-Request-Id`.
*  Stored in MDC and propagated via Feign interceptors
*  Persisted inside Outbox events for end‑to‑end traceability

Native SQL Analytics
Transfer-service includes analytical endpoints implemented via native SQL:
*  Account turnover for period
*  Top N transfers by amount

Demonstrates complex joins, aggregations, and performance‑oriented queries

### ❗ Custom Domain Exceptions
Each service uses structured exceptions:
* `CardNotFoundException`, `UserNotFoundException`, `InsufficientFundsException`, `CurrencyMismatchException` ...

All errors return structured JSON with `type`, `title`, `status`, `detail`, `instance`, `requestId`, `timestamp`

### 🧩 Technologies
### Core
* Java 17
* Gradle (multi-module)
* Spring Boot 3.1.6
* Spring Security (OAuth2 Resource Server + OAuth2 Client)
* Spring Cloud Gateway (WebFlux)
* Spring Cloud OpenFeign + OkHttp
* Spring Data JPA (Hibernate)
* Liquibase
* Spring Kafka
* JWT (JJWT, RS256, JWKS)
* Spring Retry
* Springdoc OpenAPI (Swagger UI)
* Lombok
### Infrastructure
* PostgreSQL
* Apache Kafka + ZooKeeper (Confluent images in Docker)
* Docker & Docker Compose
### 🧪 Testing
* JUnit 5
* Spring Boot Test
* Testcontainers (PostgreSQL, Kafka)
* AssertJ
* Mockito
* Awaitility

### ▶ Getting Started
Requirements:
* JDK 17+
* Docker
* Docker Compose
  
Clone repository:
```sh
git clone https://github.com/MikeReliable/bank-rest.git
```
Prepare local environment:
```sh
cp .env.example .env
```
All default local passwords/secrets are now stored in `.env.example`.
Override values in `.env` if needed:
```sh
INTERNAL_CLIENT_TRANSFER_SERVICE_SECRET
AUTH_JWT_KEYSTORE_PASSWORD
SHARED_SSL_KEYSTORE_PASSWORD
TRUSTSTORE_PASSWORD
DB_AUTH_PASSWORD
DB_CARD_PASSWORD
DB_TRANSFER_PASSWORD
DB_USERDB_PASSWORD
AUTH_RATE_LIMIT_LOGIN_MAX_REQUESTS
AUTH_RATE_LIMIT_LOGIN_WINDOW_SECONDS
AUTH_RATE_LIMIT_TOKEN_MAX_REQUESTS
AUTH_RATE_LIMIT_TOKEN_WINDOW_SECONDS
AUTH_JWT_ISSUER
AUTH_JWT_AUDIENCE_USER
AUTH_JWT_AUDIENCE_SERVICE
GATEWAY_JWT_ACCEPTED_AUDIENCES
USER_JWT_ACCEPTED_AUDIENCES
CARD_JWT_ACCEPTED_AUDIENCES
TRANSFER_JWT_ACCEPTED_AUDIENCES
```
Generate local JWT keystore (do not commit it):
```sh
keytool -genkeypair -alias auth-jwt -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore certs/auth-jwt.p12 -storepass <AUTH_JWT_KEYSTORE_PASSWORD> -validity 3650 -dname "CN=auth-service,OU=bank,O=pet,L=local,S=local,C=US"
```
Generate local TLS keystore and truststore (do not commit them):
```sh
keytool -genkeypair -alias bank-shared-tls -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore certs/shared-services.p12 -storepass <SHARED_SSL_KEYSTORE_PASSWORD> -validity 3650 -dname "CN=bank-local,OU=bank,O=pet,L=local,S=local,C=US"
keytool -exportcert -rfc -keystore certs/shared-services.p12 -storepass <SHARED_SSL_KEYSTORE_PASSWORD> -alias bank-shared-tls -file certs/shared-services.crt
keytool -importcert -noprompt -storetype PKCS12 -keystore certs/truststore.p12 -storepass <TRUSTSTORE_PASSWORD> -alias bank-shared-tls -file certs/shared-services.crt
```
Start infrastructure and services:
```sh
./gradlew :api-gateway:bootJar :auth-service:bootJar :user-service:bootJar :card-service:bootJar :transfer-service:bootJar
docker-compose up --build
```
Optional: open PostgreSQL ports for local IDE/psql access (dev-only):
```sh
docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build
```
### Swagger Endpoint
All services are accessible through the API Gateway

Single Swagger UI: [https://localhost:8080/webjars/swagger-ui](https://localhost:8080/webjars/swagger-ui)
