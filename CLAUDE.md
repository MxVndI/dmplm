# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**DiplomShop** is an educational e-commerce platform with integrated A/B testing, analytics, and AI-powered test management via Telegram. It's a microservices architecture built on Spring Boot 3.2, MongoDB, Apache Kafka, and n8n automation.

### Core Purpose
- E-commerce shop with built-in A/B testing framework
- User-specific variant assignment based on demographics
- Real-time event metrics tracking (page views, clicks, scroll depth)
- AI-driven test creation via Telegram bot (Groq LLama + Whisper)
- Email and Telegram notifications

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| **Language** | Java 21 |
| **Framework** | Spring Boot 3.2.x, Spring Security, Spring Data MongoDB |
| **Message Bus** | Apache Kafka (KRaft mode, no Zookeeper) |
| **Database** | MongoDB (7.0, multi-database) |
| **Storage** | MinIO (S3-compatible) or AWS S3 |
| **Stream Processing** | Kafka Streams (selector-service) |
| **Frontend** | Thymeleaf + vanilla CSS/JS |
| **AI / Bot** | n8n + Groq LLama-3.3-70b + Whisper |
| **Monitoring** | Prometheus + Grafana (Micrometer) |
| **Build** | Maven 3.9+, Docker, Docker Compose |

---

## Architecture: Five Microservices + Infrastructure

### 1. diplom-shop (Port 8080) — Core E-Commerce + A/B Routing
- Main Spring Boot web application; Thymeleaf UI; shopping cart; user authentication
- **Key Components**: ABInterceptor & RemoteABTestResolver (HTTP interceptor for variant resolution), FrontendController (renders variant-specific templates), MetricsService (placeholder for events), TestParticipantSyncService (Kafka consumer), ProductService, CartService, StorageService (S3 wrapper), DataInitializer (seeds DB on startup)
- **Database**: diplom_shop
- **HTTP Clients**: test-service (8081), demographic-service (8084), notification-service (8083)

### 2. diplom-test-service (Port 8081) — A/B Config & Assignment Management
- Stores ABTest configs and ABRules; resolves variant for incoming requests
- **Key Components**: ABTestService (variant assignment: sticky, weighted random), ABRuleService (matches user demographics), TestParticipantService (manages enrolled users), TestParticipantListener (Kafka consumer)
- **REST API**: POST /api/tests, GET /api/tests/{id}/resolve, PUT /api/rules
- **Database**: diplom_tests
- **Kafka Topics**: Consumes user-registered, user-profile-updates; Produces user-assigned-to-test

### 3. diplom-selector-service (Port 8082) — Kafka Streams: Real-Time User Selection
- Stream processor that matches new users against active tests in real-time
- **Key Components**: Kafka Streams topology (consumes user-registered -> queries demographic-service -> publishes user-assigned-to-test), GlobalKTable<userId, UserProfile> (in-memory cache), UserSelectionProcessor
- **Database**: diplom_tests (read-only)
- **HTTP Client**: demographic-service (8084)
- **Stream-only**: No REST controllers; REST endpoint /status for health check

### 4. diplom-demographic-service (Port 8084) — User Demographics Store
- Read-only service; stores user profiles (age, country, gender, language)
- **REST API**: GET /api/profiles/{userId}
- **Database**: diplom_demographics
- **Used By**: selector-service, test-service

### 5. diplom-notification-service (Port 8083) — Email & Telegram Broadcasts
- Sends notifications to users and admins
- **Key Components**: EmailService (SMTP: Gmail STARTTLS), TelegramService (polling), NotificationController
- **Database**: diplom_notifications
- **External Services**: Gmail SMTP (587), Telegram Bot API

### 6. n8n (Port 5678) — Low-Code Automation + Telegram AI Bot
- Telegram bot for test creation; workflow engine for analytics
- **Workflows**: telegram-ab-test-workflow.json (receives text/voice -> Groq LLama parses -> POST to test-service), metrics-ai-analyst-workflow.json (scheduled analytics)
- **AI**: Groq LLama-3.3-70b + Whisper (free tier)

### Infrastructure
- **MongoDB**: 6 databases (shop, tests, notifications, demographics, n8n state)
- **Kafka**: KRaft mode (controller + broker combined), 3 partitions, auto-create topics enabled
- **MinIO**: S3-compatible bucket storage
- **Prometheus**: Scrapes /actuator/prometheus every 15s
- **Grafana**: Pre-built A/B test dashboard; login: admin/admin

---

## Build & Run

### Docker Compose (Recommended: 11 services + infrastructure)
`ash
cd diplom
docker-compose up -d
```bash
Wait 30-60s for Kafka healthcheck. Access:
- Shop: http://localhost:8080
- Grafana: http://localhost:3000 (admin/admin)
- n8n: http://localhost:5678
- MinIO: http://localhost:9001 (minioadmin/minioadmin)
- Prometheus: http://localhost:9090

### Build Single Service
`ash
cd diplom-test-service
mvn clean package -DskipTests
java -jar target/diplom-test-service-0.0.1-SNAPSHOT.jar
```bash

### Run Tests
`ash
cd diplom
mvn test
mvn test -Dtest=DiplomApplicationTests
```bash

---

## Key Architecture Patterns

### Variant Resolution
1. User requests page -> ABInterceptor intercepts
2. Calls RemoteABTestResolver -> HTTP GET to test-service /api/tests/{testId}/resolve?userId=
3. test-service returns {testId, variant} (or null if not enrolled)
4. FrontendController renders templates/{testId}/{variant}/page.html or templates/default/page.html

### Kafka Event Flow
- **user-registered**: User signup -> selector-service & test-service consume
- **user-profile-updates**: Demographics changed -> selector-service GlobalKTable refresh
- **user-assigned-to-test**: Selector-service matches user to test -> test-service & diplom-shop consume

### Multi-Database MongoDB
Each service owns its database:
- diplom_shop: User, Product, Order, ABTest, UserTestParticipation
- diplom_tests: ABTest, ABRule, TestParticipant
- diplom_demographics: UserProfile
- diplom_notifications: Notification records

### A/B Variant Assignment (test-service)
- **Sticky**: User gets same variant always (hash-based or DB-stored)
- **Weighted Random**: Variant by probability (e.g., 70/30 split)
- **Rule-Based**: Match demographics (age range, country, gender, language)
- Stored in UserTestParticipation; compound unique index (testId, userId) prevents double-enrollment

### Metrics Extension
Three placeholders ready for implementation:
1. **UserEvent model**: eventType, eventData (Map), page, testId, variant, sessionId, userAgent, ipAddress
2. **MetricsService.recordEvent()**: Logs to stdout; uncomment userEventRepository.save(event) to persist
3. **MetricsController**: GET /api/metrics/test/{id}, GET /api/metrics/user/{id} return 501

Enable by uncommenting repository.save() and implementing aggregation logic.

### Spring Security
- Custom MongoUserDetailsService queries User by login
- BCrypt password hashing
- Cookie-based CSRF protection (XSRF-TOKEN cookie, readable by JS)
- /api/** and /internal/** bypass CSRF (server-to-server)
- Admin role via @PreAuthorize("hasRole('ADMIN')")

### S3 Storage
- MinIO (dev): http://localhost:9000, bucket: diplom-shop
- AWS S3 (prod): Leave S3_ENDPOINT empty, use AWS credentials
- Product photos uploaded via StorageService, public URLs stored in Product.photoUrl

---

## Project Structure: diplom (Main Service, 64 Java files)

### Layers
- **config/**: ABInterceptor, ABTestResolver, SecurityConfig, S3Config, MongoConfig, KafkaConfig, WebMvcConfig, DataInitializer, MetricsInterceptor
- **controller/**: FrontendController (A/B routing), AuthController, ProductController, AdminProductController, AdminUserController, AdminConfigTestController, ABTestController, MetricsController, TestServiceProxyController, DemographicProxyController, NotificationProxyController, GlobalExceptionHandler
- **service/**: ABTestService, UserService, ProductService, StorageService, CartService, TestParticipantSyncService (Kafka consumer), MetricsService, ABTestMetricsExporter, AssignmentServiceClient
- **model/**: User, Product, ABTest, UserTestParticipation, UserEvent, TestTemplate, Order, Gender enum
- **repository/**: Spring Data MongoDB repositories
- **event/**: TestParticipantEvent, UserProfileEvent (Kafka events)
- **security/**: MongoUserDetailsService
- **dto/**: UserRegistrationDto, UserUpdateDto, ProductDto (with validation)

### Resources
- **application.yml**: MongoDB, Kafka, S3, service URLs, logging
- **static/css/styles.css** (497 lines): Shared styles + A/B overrides
- **static/js/main.js** (198 lines): Placeholder for frontend metrics tracking
- **templates/**: Thymeleaf (auth/, products/, profile/, admin/, default/, variant-a/, variant-b/)

### Other Services
- **diplom-test-service** (26 files): ABTest, ABRule, TestParticipant management; variant assignment logic
- **diplom-selector-service** (12 files): Kafka Streams topology
- **diplom-notification-service** (16 files): Email (Gmail SMTP) + Telegram
- **diplom-demographic-service** (7 files): Read-only user profile API

---

## Environment Variables & Defaults

### Key Vars (docker-compose.yml sets automatically)
- MONGO_URI=mongodb://mongo:27017/diplom_shop
- KAFKA_BOOTSTRAP_SERVERS=kafka:9092
- S3_ENDPOINT=http://minio:9000 (empty for AWS S3)
- S3_ACCESS_KEY=minioadmin
- S3_SECRET_KEY=minioadmin
- TEST_SERVICE_URL=http://test-service:8081
- GROQ_API_KEY=gsk_... (free tier at console.groq.com)
- TELEGRAM_BOT_TOKEN=7466629748:...
- TELEGRAM_ADMIN_CHAT_IDS=787209390

### Credentials
- **Admin User**: login=admin, password=Admin1234! (created on startup)
- **Grafana**: admin/admin
- **MinIO**: minioadmin/minioadmin

---

## Monitoring

### Prometheus (http://localhost:9090)
- Scrapes diplom-shop /actuator/prometheus every 15s
- Metrics: JVM, HTTP, Kafka, MongoDB
- Config: diplom/monitoring/prometheus.yml

### Grafana (http://localhost:3000)
- Pre-built A/B test dashboard
- Provisioned: diplom/monitoring/grafana/provisioning/dashboards/ab-tests.json

---

## Known Limitations

1. **Metrics**: Endpoints return 501; uncomment MetricsService.recordEvent() to enable
2. **n8n Secrets**: API keys hardcoded in docker-compose.yml
3. **Selector Service**: Stream-only, no REST API
4. **Test Coverage**: Minimal; integration tested via Docker Compose
5. **Checkout**: Order entity exists but no payment flow
6. **Service Auth**: Internal calls lack authentication

---

## Common Tasks

### Debug Variant Assignment
`ash
curl -i http://localhost:8080/?userId=testuser123
# Check logs: diplom-shop -> test-service -> variant returned
```bash

### Enable Metrics Persistence
1. Uncomment userEventRepository.save(event) in MetricsService.java
2. Implement aggregation in MetricsController.java
3. Rebuild: mvn clean package -DskipTests && docker-compose up -d --build

### Add A/B Test
- Via UI: http://localhost:8080/admin/ab-tests
- Via API: curl -X POST http://localhost:8081/api/tests -d '{...}'

### Add Variant Template
1. Create: templates/{testId}/{variant}/page.html
2. Add CSS: static/css/variant-{testId}-{variant}.css
3. Reference: <link rel="stylesheet" href="/css/variant-{testId}-{variant}.css">
