# 🚀 Quick Start Checklist — DiplomShop

**Estimated Time**: 5-10 minutes (depending on internet speed for Docker images)

---

## ✅ Pre-Flight Checks

- [ ] Docker installed (`docker --version`)
- [ ] Docker Compose installed (`docker-compose --version`)
- [ ] At least 8GB RAM available
- [ ] Ports free: 8080, 8081, 8082, 8083, 8084, 3000, 9090, 9000, 27017, 9092
- [ ] Internet connection available (for Docker image pulls)

---

## 📝 Step 1: Configure Environment (2 min)

Create `.env` file in project root:

```bash
# Copy this to .env in the diplom/ directory
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
TELEGRAM_BOT_TOKEN=
TELEGRAM_ADMIN_CHAT_IDS=
NOTIFICATION_EMAIL_ENABLED=true
NOTIFICATION_TELEGRAM_ENABLED=false
```

**Gmail Setup** (if you want email notifications):
1. Go to https://myaccount.google.com/apppasswords
2. Select "Mail" & "Windows Computer"
3. Copy the 16-character password
4. Paste into MAIL_PASSWORD above

---

## 🐳 Step 2: Launch Docker Compose (3-5 min)

```bash
# Navigate to project directory
cd /path/to/diplom

# Start all services
docker-compose up -d

# Monitor startup (Kafka takes 30-60 seconds)
docker-compose logs -f

# Check all services are healthy
docker-compose ps
```

**Expected Output**:
```
NAME                    STATUS          PORTS
diplom-mongo            Up              27017/tcp
diplom-kafka            Up              9092/tcp, 29092/tcp
diplom-minio            Up              9000/tcp, 9001/tcp
diplom-shop             Up              8080/tcp
diplom-test             Up              8081/tcp
diplom-selector         Up              8082/tcp
diplom-demographic      Up              8084/tcp
diplom-notification     Up              8083/tcp
diplom-prometheus       Up              9090/tcp
diplom-grafana          Up              3000/tcp
```

---

## 🧪 Step 3: Verify Installation (1 min)

```bash
# Check shop service is running
curl -s http://localhost:8080/actuator/health

# Expected: {"status":"UP"}
```

---

## 🎯 Step 4: Access the Platform

| Service | URL | Credentials |
|---------|-----|------------|
| **Shop** | http://localhost:8080 | admin / Admin1234! |
| **Grafana** | http://localhost:3000 | admin / admin |
| **Prometheus** | http://localhost:9090 | (no auth) |
| **MinIO** | http://localhost:9001 | minioadmin / minioadmin |

---

## 🎮 Step 5: Quick Test (2 min)

### Via Browser
1. Open http://localhost:8080
2. Click "Register" (top right)
3. Create account with any credentials
4. Login with your new account
5. Browse products
6. Add product to cart
7. View your profile

### Via curl
```bash
# Get all products
curl http://localhost:8080/api/products | jq

# Get user metrics (after clicking around)
curl http://localhost:8080/api/metrics/test/test-id | jq
```

---

## 📊 Step 6: Create Your First A/B Test

### Via API
```bash
# Create a test
curl -X POST http://localhost:8081/api/tests \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Homepage Redesign Test",
    "description": "Testing new layout",
    "criteria": {
      "variantARatio": 50
    }
  }' | jq '.id'
# Copy the returned ID

# Trigger user selection
curl -X POST http://localhost:8081/api/tests/{TEST_ID}/trigger

# Check participants
curl http://localhost:8081/api/tests/{TEST_ID}/stats
```

### Via Admin Panel
1. Go to http://localhost:8080/admin/ab-tests
2. Click "Create Test"
3. Fill in name, description, criteria
4. Click "Trigger Selection"
5. View participants in "Participants" tab

---

## 📈 Step 7: Monitor in Grafana

1. Go to http://localhost:3000
2. Click "Dashboards" → "DiplomShop A/B Testing Dashboard"
3. View real-time metrics:
   - Request rates
   - Latency (P95)
   - JVM memory usage

---

## ⚠️ Common Issues & Fixes

| Issue | Solution |
|-------|----------|
| **Port already in use** | Change port in docker-compose.yml, e.g. `8080:8080` → `18080:8080` |
| **MongoDB connection refused** | Wait 60 seconds, then `docker-compose restart mongo` |
| **Kafka not healthy** | Check logs: `docker-compose logs kafka`. If stuck, run `docker-compose down -v && docker-compose up -d` |
| **Services crashing** | Check RAM (need 8GB), reduce container memory in docker-compose.yml |
| **Email not sending** | Verify MAIL_USERNAME & MAIL_PASSWORD, check notification-service logs |

---

## 🔧 Useful Commands

```bash
# View logs for a service
docker-compose logs -f shop

# Restart a service
docker-compose restart test

# Stop all services
docker-compose down

# Completely reset (removes volumes)
docker-compose down -v

# Rebuild after code changes
docker-compose build && docker-compose up -d

# Access MongoDB directly
docker exec -it diplom-mongo mongosh

# List Kafka topics
docker exec diplom-kafka kafka-topics --bootstrap-server kafka:9092 --list

# Consume Kafka messages
docker exec -it diplom-kafka kafka-console-consumer \
  --bootstrap-server kafka:9092 \
  --topic user-profiles --from-beginning
```

---

## 📚 Next Steps

1. **Read documentation**:
   - FINAL_SETUP_GUIDE.md (comprehensive guide)
   - IMPLEMENTATION_ROADMAP.md (design specifications)
   - PROJECT_COMPLETION_REPORT.md (what's implemented)

2. **Customize the platform**:
   - Upload variant templates in Admin → Templates
   - Create custom A/B tests
   - Configure email settings
   - Connect Telegram bot (optional)

3. **Integrate with your systems**:
   - Query `/api/metrics/test/{testId}` for test results
   - Send notifications via `/api/campaigns`
   - Track user behavior via `/api/metrics/user/{userId}`

---

## ✨ You're All Set!

The platform is fully functional and ready to use. Start with the browser and explore, or use the API for programmatic access.

**Questions?** Check the documentation files or review the source code — it's well-commented.

Happy A/B testing! 🎉
