# DiplomShop — Final Setup & Launch Guide

## ✅ Implementation Status

### Core Features (COMPLETE)
- [x] **User Management**: Registration, authentication, profiles with demographics
- [x] **Product Management**: Product CRUD, inventory, image storage (S3/MinIO)
- [x] **Shopping Cart & Checkout**: Session-based cart, order persistence, stock management
- [x] **A/B Testing Framework**: Test creation, variant assignment (sticky, weighted, rule-based)
- [x] **Real-Time Metrics Collection**: Frontend event tracking (PAGE_VIEW, CLICK, SCROLL_DEPTH, etc)
- [x] **Statistical Analysis**: Funnel analysis, conversion metrics, significance testing (z-test)
- [x] **Kafka Event Streaming**: User registration, profile updates, test assignments
- [x] **Kafka Streams Processing**: Real-time user selection with GlobalKTable state store
- [x] **Email Notifications**: Campaign creation, bulk email dispatch via Gmail SMTP
- [x] **Telegram Notifications**: Message delivery to users with chat ID linking
- [x] **Monitoring**: Prometheus metrics export, Grafana dashboards
- [x] **Docker Orchestration**: Complete docker-compose with all 11 services

### Microservices Architecture
1. **diplom-shop** (Port 8080): Main e-commerce + A/B routing
2. **diplom-test-service** (Port 8081): Test config & variant assignment management
3. **diplom-selector-service** (Port 8082): Kafka Streams real-time user selection
4. **diplom-demographic-service** (Port 8084): User profile & demographics store
5. **diplom-notification-service** (Port 8083): Email & Telegram campaign dispatch
6. **Infrastructure**: MongoDB, Kafka (KRaft), MinIO (S3), Prometheus, Grafana

---

## 🚀 Quick Start: Docker Compose

### Prerequisites
- Docker & Docker Compose (v3.8+)
- 8GB RAM minimum
- Ports 8080, 8081, 8082, 8083, 8084, 3000, 9090, 9000, 27017, 9092 available

### Step 1: Environment Setup
Create `.env` file in root directory:
```bash
# Gmail SMTP (get App Password from https://myaccount.google.com/apppasswords)
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# Telegram Bot (optional, leave empty to disable)
TELEGRAM_BOT_TOKEN=your-bot-token-here
TELEGRAM_ADMIN_CHAT_IDS=your-chat-id-here

# Notification settings
NOTIFICATION_EMAIL_ENABLED=true
NOTIFICATION_TELEGRAM_ENABLED=false
```

### Step 2: Build & Start Services
```bash
# Build all services (one-time, or after code changes)
docker-compose build

# Start all services in background
docker-compose up -d

# Wait for Kafka healthcheck (~30-60 seconds)
docker-compose logs -f kafka | grep "started"

# View logs for any service
docker-compose logs -f shop      # Main shop
docker-compose logs -f test      # Test service
docker-compose logs -f selector  # Selector service
```

### Step 3: Access the Platform
```
🏪 Shop:      http://localhost:8080
📊 Grafana:   http://localhost:3000 (admin/admin)
📈 Prometheus: http://localhost:9090
💾 MinIO:     http://localhost:9001 (minioadmin/minioadmin)
```

### Step 4: Initial Data
Databases & demo user are initialized automatically:
- **Admin User**: login=`admin`, password=`Admin1234!`
- **Demo Users** (in demographic service): demo-user-1, demo-user-2, demo-user-3

---

## 🧪 Test the System End-to-End

### 1. Register a New User
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "login=testuser&password=Password123!&firstName=Test&lastName=User&email=test@example.com&age=30&country=US&language=en&gender=MALE"
```

### 2. View Products
```bash
curl http://localhost:8080/api/products
```

### 3. Create an A/B Test (as admin)
```bash
# Login first to get session cookie, then:
curl -X POST http://localhost:8081/api/tests \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Homepage Redesign",
    "description": "Testing new layout",
    "criteria": {
      "ageMin": 18,
      "ageMax": 65,
      "countries": ["US", "CA"],
      "variantARatio": 50
    }
  }'
```

### 4. Trigger User Selection
```bash
# Replace {testId} with the ID from step 3
curl -X POST http://localhost:8081/api/tests/{testId}/trigger
```

### 5. Monitor Metrics
Navigate to http://localhost:3000 and view the "DiplomShop A/B Testing Dashboard" to see:
- HTTP request rates
- P95 latency
- JVM memory usage
- Kafka broker health

---

## 🔧 Development: Local Build (Without Docker)

### Prerequisites
- Java 21
- Maven 3.9+
- MongoDB 7.0
- Kafka 3.5+ (with KRaft mode)
- MinIO (optional, can skip S3 for local dev)

### Build All Services
```bash
cd /path/to/diplom

# Build parent POM + all 5 microservices
mvn clean package -DskipTests

# Output: target/ in each service directory contains the JAR
```

### Run Individual Services
```bash
# Terminal 1: Shop
cd diplom-shop && java -jar target/diplom-shop-*.jar

# Terminal 2: Test Service
cd diplom-test-service && java -jar target/diplom-test-service-*.jar

# Terminal 3: Selector Service (Kafka Streams)
cd diplom-selector-service && java -jar target/diplom-selector-service-*.jar

# Terminal 4: Demographic Service
cd diplom-demographic-service && java -jar target/diplom-demographic-service-*.jar

# Terminal 5: Notification Service
cd diplom-notification-service && java -jar target/diplom-notification-service-*.jar
```

---

## 📐 Architecture Overview

### Variant Resolution Flow
```
1. User requests page (authenticated) → AbInterceptor
2. ABInterceptor → AssignmentServiceClient
3. AssignmentServiceClient:
   - Tries remote selector-service (if URL configured)
   - Falls back to local ABTestService
4. ABTestService returns UserTestParticipation (variant A/B/C/D)
5. FrontendController renders:
   - template/{testId}/{variant}/page.html if template uploaded
   - variant-{variant}/home.html (default templates)
   - default/home.html if no active test
```

### Kafka Event Flow
```
user-registered topic
↓
selector-service (Kafka Streams GlobalKTable)
├─ Scans user-profiles state store
├─ Applies TestCriteria filters (age, country, gender, language)
├─ Queries demographic-service for extended attributes
├─ Randomly assigns A/B variants
├─ Saves TestParticipantEntity to MongoDB (diplom_tests.test_participants)
└─ Publishes TestParticipantEvent → test-participants-result topic
  ↓
  test-service & shop consume
  ↓
  Update UserTestParticipationEntity in diplom_tests & diplom_shop
```

### Metrics & Analytics
```
Frontend (main.js) → /api/metrics/event (MetricsController)
↓
MetricsService.recordEvent() → MongoDB (diplom_shop.user_events)
↓
MetricsController.getTestMetrics(testId) → Statistical Analysis:
├─ Per-variant event aggregation
├─ Unique user counts
├─ Funnel analysis (PAGE_VIEW → ADD_TO_CART → CHECKOUT_START → ORDER)
├─ Order data from OrderEntity
└─ Two-proportion z-test for significance (p < 0.05)
```

---

## 🛠️ Common Tasks

### Add a New Product (As Admin)
```bash
curl -X POST http://localhost:8080/admin/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "price": 999.99,
    "description": "High-performance laptop",
    "availableQuantity": 50
  }'
```

### Create Variant Templates
1. Go to http://localhost:8080/admin/templates
2. Select test & variant
3. Upload custom HTML file (e.g., `variant-a-homepage.html`)
4. File stored in MinIO; shop fetches & renders on variant-specific requests

### View A/B Test Results
1. http://localhost:8080/admin/metrics
2. Select test
3. View per-variant:
   - Event counts (page views, clicks, etc)
   - Unique users
   - Conversion rates
   - Statistical significance

### Send Notification Campaign
```bash
# Create campaign
curl -X POST http://localhost:8083/api/campaigns \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Welcome Email",
    "channel": "EMAIL",
    "subject": "Welcome to DiplomShop!",
    "body": "...",
    "targetType": "ALL"
  }'

# Send it
curl -X POST http://localhost:8083/api/campaigns/{campaignId}/send
```

### Monitor Kafka Topics
```bash
# List topics
docker exec diplom-kafka kafka-topics --bootstrap-server kafka:9092 --list

# Consume messages (real-time)
docker exec -it diplom-kafka kafka-console-consumer \
  --bootstrap-server kafka:9092 \
  --topic user-profiles \
  --from-beginning
```

---

## 🐛 Troubleshooting

### Services Won't Start
```bash
# Check logs
docker-compose logs service-name

# Common causes:
# - MongoDB not ready: wait 30s, retry
# - Port already in use: change port in docker-compose.yml
# - Kafka broker crash: check Kafka logs, ensure KRaft is initialized
```

### Kafka Not Initializing
```bash
# KRaft mode requires initial startup sequence
# If broker fails to start:
docker-compose down -v  # Remove volumes
docker-compose up -d kafka  # Start Kafka first
sleep 60  # Wait for KRaft initialization
docker-compose up -d  # Start all services
```

### Metrics Not Appearing
- Check browser console (F12) for JavaScript errors
- Verify `/api/metrics/event` endpoint is reachable: `curl -X POST http://localhost:8080/api/metrics/event -H "Content-Type: application/json" -d '{"eventType":"TEST"}'`
- Check shop service logs for user event recording

### Email Not Sending
- Verify MAIL_USERNAME & MAIL_PASSWORD in .env
- Gmail requires App Password (not regular password) from https://myaccount.google.com/apppasswords
- Check notification-service logs: `docker-compose logs notification`
- Verify recipient email in user profile

---

## 📚 API Reference

### Shop Service (8080)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/register` | Register new user |
| POST | `/api/products` | Create product (admin) |
| GET | `/api/products` | List products |
| POST | `/api/cart/add` | Add to cart |
| POST | `/api/cart/checkout` | Checkout |
| GET | `/api/metrics/test/{testId}` | Test summary & stats |
| GET | `/internal/users` | Internal: all users |
| GET | `/internal/users/{userId}` | Internal: single user |

### Test Service (8081)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/tests` | Create test |
| GET | `/api/tests` | List tests |
| GET | `/api/tests/{id}` | Get test |
| POST | `/api/tests/{id}/trigger` | Trigger selection |
| GET | `/api/tests/{id}/participants` | Get participants |
| GET | `/api/tests/{id}/stats` | Participant stats |

### Demographic Service (8084)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/demographics` | All users' demographics |
| GET | `/api/demographics/{userId}` | Single user demographics |
| POST | `/api/demographics/bulk` | Bulk fetch |
| POST | `/api/demographics` | Upsert demographics |

### Notification Service (8083)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/campaigns` | Create campaign |
| GET | `/api/campaigns` | List campaigns |
| POST | `/api/campaigns/{id}/send` | Send campaign |
| GET | `/api/campaigns/{id}/stats` | Delivery stats |

---

## 📝 Notes

- All passwords are hashed with BCrypt
- User IDs, product IDs, test IDs are MongoDB ObjectIds (24-char hex)
- Session-based cart is not persisted (cleared on logout)
- A/B variant assignment is sticky (stored in UserTestParticipationEntity)
- Metrics are real-time (no aggregation delay)
- Test expiration is checked every 60 seconds
- Notification dispatch is asynchronous (@Async on NotificationDispatcher)

---

## 🎯 Next Steps (Optional Enhancements)

1. **n8n Integration**: Add Telegram bot for test creation via voice
2. **Advanced Segmentation**: Add rule-based audience targeting
3. **Custom Events**: Allow admins to define custom event types
4. **Webhooks**: Notify external systems on test completion
5. **API Authentication**: Add API key or OAuth2 for service-to-service auth
6. **Performance Optimization**: Add Redis for cart caching
7. **Mobile App**: Native iOS/Android app for test monitoring

---

## 📞 Support

For issues:
1. Check Docker logs: `docker-compose logs [service-name]`
2. Verify all services are healthy: `docker-compose ps`
3. Check .env file for required variables
4. Ensure MongoDB, Kafka are initialized (wait 60+ seconds on first start)
