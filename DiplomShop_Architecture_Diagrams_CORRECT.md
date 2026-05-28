# DiplomShop Architecture Diagrams (Corrected)

## 1. Общая архитектура системы (High-Level)

```mermaid
graph TB
    Browser["🌐 Web Browser<br/>localhost:8080"]
    
    subgraph "Microservices Tier"
        Shop["🛍️ diplom-shop<br/>Port 8080<br/>Spring Boot<br/>E-commerce + UI<br/>(Thymeleaf + vanilla JS)"]
        Test["🧪 diplom-test-service<br/>Port 8081<br/>Spring Boot<br/>A/B Management"]
        Selector["📊 diplom-selector-service<br/>Port 8082<br/>Spring Boot<br/>Kafka Streams"]
        Demographic["👤 diplom-demographic-service<br/>Port 8084<br/>Spring Boot<br/>User Profiles"]
        Notification["📧 diplom-notification-service<br/>Port 8083<br/>Spring Boot<br/>Email/Telegram"]
        Clustering["🤖 diplom-clustering-service<br/>Port 8085<br/>Python FastAPI<br/>K-means ML"]
    end
    
    subgraph "Data & Message Tier"
        MongoDB["🗄️ MongoDB 7.0<br/>6 Databases<br/>diplom_shop, diplom_tests,<br/>diplom_demographics, etc."]
        Kafka["📬 Apache Kafka<br/>KRaft Mode<br/>5 Topics"]
    end
    
    subgraph "Storage & External"
        MinIO["🗂️ MinIO S3<br/>Product Images"]
        Gmail["📨 Gmail SMTP<br/>Email Delivery"]
        Telegram["💬 Telegram Bot API<br/>Admin Notifications"]
    end
    
    subgraph "Monitoring & Analytics"
        Prometheus["📈 Prometheus<br/>Metrics Collection"]
        Grafana["📊 Grafana Dashboards<br/>Visualization"]
    end
    
    subgraph "CI/CD & Automation"
        n8n["⚙️ n8n Workflows<br/>Automation"]
    end
    
    Browser -->|"HTTP<br/>GET /products<br/>GET /?userId=X&testId=Y"| Shop
    
    Shop -->|"REST API"| Test
    Shop -->|"REST API"| Demographic
    Shop -->|"Events"| Kafka
    
    Test -->|"REST API"| Demographic
    Test -->|"REST API"| Clustering
    Test -->|"Events"| Kafka
    
    Selector -->|"REST API"| Demographic
    Selector -->|"Consume/Produce"| Kafka
    
    Notification -->|"REST API"| Shop
    Notification -->|"Consume"| Kafka
    
    Shop -->|"Read/Write"| MongoDB
    Test -->|"Read/Write"| MongoDB
    Demographic -->|"Read/Write"| MongoDB
    Notification -->|"Read/Write"| MongoDB
    Selector -->|"State Store"| MongoDB
    Clustering -->|"Load Model"| MongoDB
    
    Shop -->|"Upload/Download"| MinIO
    
    Notification -->|"Send"| Gmail
    Notification -->|"Send"| Telegram
    
    Shop -->|"Metrics"| Prometheus
    Test -->|"Metrics"| Prometheus
    Selector -->|"Metrics"| Prometheus
    Demographic -->|"Metrics"| Prometheus
    Notification -->|"Metrics"| Prometheus
    Clustering -->|"Metrics"| Prometheus
    
    Prometheus -->|"Data"| Grafana
    
    n8n -->|"Trigger"| Test
    n8n -->|"Monitor"| Prometheus

    style Shop fill:#4CAF50
    style Test fill:#2196F3
    style Selector fill:#9C27B0
    style Demographic fill:#FF5722
    style Notification fill:#00BCD4
    style Clustering fill:#FFC107
    style MongoDB fill:#13AA52
    style Kafka fill:#000000,color:#fff
    style Grafana fill:#FF9830
    style Prometheus fill:#E34234
```

---

## 2. Docker Compose структура

```mermaid
graph LR
    subgraph "Docker Compose Network (diplom-network)"
        subgraph "Databases"
            db1["🗄️ MongoDB<br/>diplom-shop<br/>:27017"]
            db2["🗄️ MongoDB<br/>diplom-tests<br/>:27017"]
            db3["🗄️ MongoDB<br/>diplom-demographics<br/>:27017"]
        end
        
        subgraph "Message Bus"
            kafka["📬 Kafka Broker<br/>KRaft Controller<br/>:9092"]
        end
        
        subgraph "Java Microservices"
            shop["🛍️ shop:8080<br/>Spring Boot"]
            test["🧪 test-service:8081<br/>Spring Boot"]
            selector["📊 selector:8082<br/>Spring Boot"]
            demographic["👤 demographic:8084<br/>Spring Boot"]
            notification["📧 notification:8083<br/>Spring Boot"]
        end
        
        subgraph "Python Services"
            clustering["🤖 clustering:8085<br/>FastAPI"]
        end
        
        subgraph "Monitoring"
            prom["📈 Prometheus:9090"]
            grafana["📊 Grafana:3000"]
        end
        
        subgraph "Automation"
            n8n_svc["⚙️ n8n:5678"]
        end
        
        subgraph "Storage"
            minio["🗂️ MinIO:9001<br/>S3 Compatible"]
        end
    end
    
    db1 -.->|"mongodb://"| shop
    db2 -.->|"mongodb://"| test
    db3 -.->|"mongodb://"| demographic
    
    kafka -.->|"bootstrap.servers"| shop
    kafka -.->|"bootstrap.servers"| selector
    kafka -.->|"bootstrap.servers"| test
    kafka -.->|"bootstrap.servers"| notification
    
    shop -.->|"REST HTTP"| test
    shop -.->|"REST HTTP"| demographic
    test -.->|"REST HTTP"| demographic
    test -.->|"REST HTTP"| clustering
    
    selector -.->|"REST HTTP"| demographic
    
    shop -.->|"S3 API"| minio
    
    shop -.->|"metrics"| prom
    test -.->|"metrics"| prom
    selector -.->|"metrics"| prom
    demographic -.->|"metrics"| prom
    notification -.->|"metrics"| prom
    clustering -.->|"metrics"| prom
    
    prom -.->|"scrape data"| grafana
    
    n8n_svc -.->|"webhook"| test
    
    style kafka fill:#000000,color:#fff
    style shop fill:#4CAF50
    style test fill:#2196F3
    style selector fill:#9C27B0
    style demographic fill:#FF5722
    style notification fill:#00BCD4
    style clustering fill:#FFC107
    style grafana fill:#FF9830
    style prom fill:#E34234
```

---

## 3. Взаимодействие микросервисов (Sync REST)

```mermaid
graph TD
    Client["👤 Client Request<br/>GET /?userId=user123&testId=test"]
    
    subgraph "Synchronous REST Calls"
        Shop["🛍️ diplom-shop<br/>Port 8080<br/>ABInterceptor"]
        Test["🧪 diplom-test-service<br/>Port 8081"]
        Demographic["👤 diplom-demographic-service<br/>Port 8084"]
        Clustering["🤖 diplom-clustering-service<br/>Port 8085"]
    end
    
    Client -->|"Request intercepted"| Shop
    Shop -->|"HTTP GET /api/tests/{id}/resolve?userId=X"| Test
    
    Test -->|"1️⃣ HTTP GET /api/profiles/{userId}"| Demographic
    
    Demographic -->|"Returns: age, country,<br/>gender, language, interests"| Test
    
    Test -->|"2️⃣ HTTP POST /api/cluster/assign<br/>{userId, features: {...}}"| Clustering
    
    Clustering -->|"Returns:<br/>{clusterId: 0-3,<br/>variant: A|B,<br/>distance: 0.45}"| Test
    
    Test -->|"Save to DB<br/>Return variant"| Shop
    
    Shop -->|"Render variant template<br/>templates/{testId}/{variant}/"| Client
    
    style Shop fill:#4CAF50
    style Test fill:#2196F3
    style Demographic fill:#FF5722
    style Clustering fill:#FFC107
    style Client fill:#90EE90
```

---

## 4. Асинхронная обработка через Kafka

```mermaid
graph LR
    subgraph "Event Producers"
        ShopProd["🛍️ diplom-shop<br/>Publishes: user-events"]
        DemoProd["👤 demographic-service<br/>Publishes: user-profiles"]
        SelectorProd["📊 selector-service<br/>Publishes:<br/>user-segment-changes<br/>test-selection-requests"]
        TestProd["🧪 test-service<br/>Publishes:<br/>test-participants-result"]
    end
    
    subgraph "Kafka Broker (KRaft)"
        Topic1["📬 user-events<br/>3 partitions"]
        Topic2["📬 user-profiles<br/>compacted"]
        Topic3["📬 user-segment-changes<br/>3 partitions"]
        Topic4["📬 test-selection-requests<br/>3 partitions"]
        Topic5["📬 test-participants-result<br/>3 partitions"]
    end
    
    subgraph "Event Consumers"
        SelectorCons["📊 selector-service<br/>Consumes: user-events<br/>user-profiles (GlobalKTable)"]
        TestCons["🧪 test-service<br/>Consumes: user-segment-changes<br/>test-selection-requests"]
        NotifCons["📧 notification-service<br/>Consumes: user-events<br/>test-participants-result"]
        ShopCons["🛍️ diplom-shop<br/>Consumes: test-participants-result"]
    end
    
    ShopProd -->|"Publish"| Topic1
    DemoProd -->|"Publish"| Topic2
    SelectorProd -->|"Publish"| Topic3
    SelectorProd -->|"Publish"| Topic4
    TestProd -->|"Publish"| Topic5
    
    Topic1 -->|"Subscribe"| SelectorCons
    Topic2 -->|"GlobalKTable cache"| SelectorCons
    Topic3 -->|"Subscribe"| TestCons
    Topic4 -->|"Subscribe"| TestCons
    
    Topic1 -->|"Subscribe"| NotifCons
    Topic5 -->|"Subscribe"| NotifCons
    
    Topic5 -->|"Subscribe"| ShopCons
    
    style Topic1 fill:#000000,color:#fff
    style Topic2 fill:#000000,color:#fff
    style Topic3 fill:#000000,color:#fff
    style Topic4 fill:#000000,color:#fff
    style Topic5 fill:#000000,color:#fff
    style SelectorCons fill:#9C27B0
    style TestCons fill:#2196F3
    style NotifCons fill:#00BCD4
    style ShopCons fill:#4CAF50
```

---

## 5. REST + Kafka Integration Flow

```mermaid
graph TD
    User["👤 User Request<br/>GET /?userId=user123"]
    
    User -->|"HTTP Sync"| Shop["🛍️ diplom-shop<br/>ABInterceptor"]
    
    Shop -->|"Sync: REST"| Test["🧪 test-service"]
    Test -->|"Sync: REST"| Demographic["👤 demographic-service"]
    Test -->|"Sync: REST"| Clustering["🤖 clustering-service"]
    
    Clustering -->|"Response: variant"| Test
    Test -->|"Response: variant"| Shop
    
    Shop -->|"Publish async"| Kafka["📬 Kafka<br/>user-events topic"]
    
    Kafka -->|"Consume async"| Selector["📊 selector-service<br/>Updates state"]
    Kafka -->|"Consume async"| Notification["📧 notification-service"]
    
    Shop -->|"HTML Response<br/>(Thymeleaf)"| Browser["✅ Web Browser<br/>Variant-specific page"]
    
    Selector -->|"Publish: segment-changes"| Kafka
    Notification -->|"Send: Email/Telegram"| External["📨 Gmail<br/>💬 Telegram"]
    
    style Shop fill:#4CAF50
    style Test fill:#2196F3
    style Clustering fill:#FFC107
    style Demographic fill:#FF5722
    style Kafka fill:#000000,color:#fff
    style Selector fill:#9C27B0
    style Notification fill:#00BCD4
    style Browser fill:#90EE90
```

---

## 6. Полный Data Flow: От запроса до A/B варианта

```mermaid
graph TD
    Start["👤 User Request<br/>GET /?userId=user123&testId=test-checkout"]
    
    Start -->|"1. Interceptor catches"| Shop["🛍️ diplom-shop<br/>ABInterceptor"]
    
    Shop -->|"2. REST GET /api/tests/test-checkout/resolve?userId=user123"| Test["🧪 test-service"]
    
    Test -->|"3. Check UserTestParticipation<br/>(sticky)"| TestDB[("💾 MongoDB<br/>diplom_tests")]
    
    TestDB -->|"Not found"| Test
    
    Test -->|"4. REST GET /api/profiles/user123"| Demographic["👤 demographic-service"]
    
    Demographic -->|"Fetch from DB"| DemoDB[("💾 MongoDB<br/>diplom_demographics")]
    
    DemoDB -->|"Profile data"| Demographic
    Demographic -->|"Profile: age, country, gender"| Test
    
    Test -->|"5. Evaluate ABRule<br/>(age >= 18, country=US)"| Decision{{"Match<br/>rule?"}}
    
    Decision -->|"No"| Reject["❌ Not eligible"]
    Decision -->|"Yes"| Cluster["🤖 Call clustering"]
    
    Test -->|"6. REST POST /api/cluster/assign<br/>{userId, features: {...}}"| Clustering["🤖 clustering-service"]
    
    Clustering -->|"Load K-means model"| Memory["🧠 Model cache"]
    Memory -->|"Normalize features<br/>StandardScaler"| Normalize["⚙️ Normalization"]
    
    Normalize -->|"Compute distance<br/>to centroids"| Distance["📏 Euclidean distance"]
    
    Distance -->|"Find nearest"| Assign["🎯 Assign cluster"]
    Assign -->|"Map: 0,1→A; 2,3→B"| Result{{"clusterId<br/>→ Variant"}}
    
    Result -->|"Cluster 0,1"| VariantA["📌 Variant A<br/>distance=0.45"]
    Result -->|"Cluster 2,3"| VariantB["📌 Variant B"]
    
    VariantA -->|"Response"| Test
    VariantB -->|"Response"| Test
    
    Test -->|"7. Save in DB<br/>(testId, userId, variant, clusterId)"| TestDB
    
    Test -->|"8. Return to client"| Shop
    
    Shop -->|"9. Publish event: user-events"| Kafka["📬 Kafka"]
    
    Kafka -->|"10. Consumed by selector"| Selector["📊 selector-service"]
    
    Shop -->|"11. Render Thymeleaf template<br/>templates/{testId}/{variant}/"| Response["🎨 HTML with variant CSS/JS"]
    
    Response -->|"HTTP Response"| Browser["✅ Web Browser<br/>User sees variant"]
    
    style Shop fill:#4CAF50
    style Test fill:#2196F3
    style Clustering fill:#FFC107
    style Demographic fill:#FF5722
    style Kafka fill:#000000,color:#fff
    style Browser fill:#90EE90
```

---

## 7. Процесс завершения A/B теста

```mermaid
graph TD
    Test["🧪 A/B Test ACTIVE<br/>expiresAt = now + 1h"]
    
    Timer["⏱️ Scheduler<br/>every 60 seconds"]
    
    Timer -->|"Check expiration"| Check{{"now >= expiresAt?"}}
    
    Check -->|"No"| Wait["⏳ Continue running"]
    Check -->|"Yes"| Aggregate["📊 Aggregate metrics"]
    
    Aggregate -->|"Count variant_a<br/>Count variant_b<br/>Conversions"| Calc["🧮 Calculate<br/>conversion rates"]
    
    Calc -->|"Create TestArchiveEntity"| Archive["📁 Save to MongoDB<br/>test_archives collection"]
    
    Archive -->|"Notify admin"| Telegram["💬 TelegramService<br/>notifyTestCompletion"]
    
    Telegram -->|"Send message"| TelegramAPI["🔔 Telegram Bot API"]
    TelegramAPI -->|"✅ Test 'Checkout' expired.<br/>Variant A: 245 users, 13% conversion<br/>Variant B: 255 users, 19% conversion"| Admin["👨‍💼 Admin receives"]
    
    Archive -->|"Clear participants"| Clear["🗑️ DELETE from<br/>UserTestParticipation"]
    
    Clear -->|"Update status"| Complete["✅ status = COMPLETED"]
    
    Complete -->|"Publish event"| Kafka["📬 Kafka<br/>test-completed"]
    
    Kafka -->|"Consumed by"| Notification["📧 notification-service"]
    
    Notification -->|"Send via SMTP"| Email["📨 Gmail"]
    Email -->|"✉️ Test results"| Owner["👨‍💼 Test Owner"]
    
    style Test fill:#2196F3
    style Telegram fill:#00BCD4
    style Archive fill:#FF9800
    style Complete fill:#4CAF50
    style Admin fill:#90EE90
```

---

## 8. K-means кластеризация flow

```mermaid
graph LR
    User["👤 User<br/>userId=user123"]
    
    User -->|"POST /api/cluster/assign<br/>HTTP JSON"| API["🔌 diplom-clustering-service<br/>FastAPI"]
    
    API -->|"Request body"| Features["📊 8 Features vector<br/>visitCount7Days: 5<br/>purchaseCount: 2<br/>totalSpent: 150.5<br/>cartAddCount: 8<br/>productViewCount: 20<br/>cartAbandoned: 0<br/>daysSinceLastEvent: 2.5<br/>hoursSinceLastCart: 6.0"]
    
    Features -->|"Apply StandardScaler<br/>mean=0, std=1"| Normalize["⚙️ Normalization<br/>[0.25, -0.5, 0.8, ...]"]
    
    Normalize -->|"Load from disk"| Model["🧠 K-means Model<br/>n_clusters=4<br/>4 centroids"]
    
    Model -->|"Compute Euclidean<br/>distance to each"| Distance["📏 Distances<br/>d_0=0.45<br/>d_1=0.82<br/>d_2=1.20<br/>d_3=2.10"]
    
    Distance -->|"min(distances)"| Assign["🎯 min_distance=0.45<br/>cluster=0"]
    
    Assign -->|"Map cluster"| Mapping["📌 Strategy:<br/>0,1 → A<br/>2,3 → B<br/>Result: variant=A"]
    
    Mapping -->|"Return JSON"| Response["✅ HTTP Response<br/>{clusterId: 0,<br/>variant: 'A',<br/>distance: 0.45}"]
    
    Response -->|"Back to test-service"| Test["🧪 diplom-test-service"]
    
    style API fill:#FFC107
    style Features fill:#FF9800
    style Model fill:#673AB7
    style Distance fill:#2196F3
    style Mapping fill:#4CAF50
    style Response fill:#90EE90
```

---

## 9. Мониторинг и Alerting

```mermaid
graph TB
    subgraph "Data Collection (Micrometer)"
        JVM["☕ JVM Metrics<br/>Memory, GC, Threads"]
        HTTP["🌐 HTTP Metrics<br/>RPS, Latency, Errors"]
        Kafka["📬 Kafka Metrics<br/>Consumer Lag"]
        MongoDB["🗄️ MongoDB Metrics<br/>Connections"]
    end
    
    subgraph "Prometheus"
        Scrape["📈 Prometheus<br/>Scraper (every 15s)<br/>Port 9090"]
        TSDB["💾 Time Series DB<br/>7 days retention"]
    end
    
    subgraph "Grafana"
        Dashboard1["📊 A/B Testing<br/>Test count, conversions"]
        Dashboard2["📊 System Health<br/>CPU, Memory"]
        Dashboard3["📊 API Performance<br/>RPS, Latency P95"]
    end
    
    subgraph "Alerting"
        Rules["⚠️ Alert Rules<br/>if P95 > 500ms<br/>if ErrorRate > 2%"]
        AlertMgr["🔔 Notifications"]
    end
    
    JVM -->|"GET /actuator/prometheus"| Scrape
    HTTP -->|"GET /actuator/prometheus"| Scrape
    Kafka -->|"GET /actuator/prometheus"| Scrape
    MongoDB -->|"GET /actuator/prometheus"| Scrape
    
    Scrape -->|"Store"| TSDB
    
    TSDB -->|"PromQL query"| Dashboard1
    TSDB -->|"PromQL query"| Dashboard2
    TSDB -->|"PromQL query"| Dashboard3
    
    TSDB -->|"Evaluate"| Rules
    Rules -->|"Fire if true"| AlertMgr
    
    AlertMgr -->|"Alert"| OnCall["👨‍⚖️ Engineer"]
    
    style Scrape fill:#E34234
    style TSDB fill:#E34234,color:#fff
    style Dashboard1 fill:#FF9830
    style Dashboard2 fill:#FF9830
    style Dashboard3 fill:#FF9830
    style AlertMgr fill:#FF5722
```

---

## 10. Deployment Pipeline (docker-compose)

```mermaid
graph LR
    Dev["💻 Developer<br/>git push"]
    
    Dev --> GitHub["🐙 GitHub Repo"]
    
    GitHub -->|"Webhook trigger"| Build["🔧 GitHub Actions<br/>Maven: mvn clean package"]
    
    Build -->|"mvn package"| JAR["📦 JAR artifacts"]
    
    JAR -->|"docker build"| Docker["🐳 Docker Build<br/>Multi-stage<br/>maven → temurin:21"]
    
    Docker -->|"Build image"| LocalTest["📦 Image ready<br/>ghcr.io/repo/service:latest"]
    
    LocalTest -->|"docker-compose pull"| DevEnv["🐳 docker-compose up -d<br/>Local Development"]
    
    DevEnv -->|"Start containers"| Services["6 Microservices<br/>+ MongoDB<br/>+ Kafka<br/>+ Grafana"]
    
    Services -->|"Integration tests"| Tests["✅ Testcontainers<br/>Test Suite"]
    
    Tests -->|"Pass"| Ready["✅ Ready for use"]
    
    style Build fill:#4CAF50
    style Docker fill:#2196F3
    style LocalTest fill:#FF9800
    style DevEnv fill:#673AB7
    style Ready fill:#4CAF50
```

---

## Компоненты (6 микросервисов)

| Сервис | Порт | Язык | Ответственность |
|--------|------|------|-----------------|
| diplom-shop | 8080 | Java | E-commerce, A/B маршрутизация, UI |
| diplom-test-service | 8081 | Java | Управление A/B тестами, вариант-выбор |
| diplom-selector-service | 8082 | Java | Kafka Streams, сегментация пользователей |
| diplom-demographic-service | 8084 | Java | Хранилище профилей пользователей |
| diplom-notification-service | 8083 | Java | Email, Telegram уведомления |
| diplom-clustering-service | 8085 | Python | K-means ML, кластеризация |

---

## Kafka топики (5)

| Топик | Партиции | Использование |
|-------|----------|---|
| user-events | 3 | События пользователей (page view, purchase, add to cart) |
| user-profiles | 1 (compact) | Демографические данные (GlobalKTable) |
| user-segment-changes | 3 | События изменения сегмента пользователя |
| test-selection-requests | 3 | Запросы на отбор пользователя в тест |
| test-participants-result | 3 | Результат назначения пользователя варианту |

---

## Базы данных MongoDB (3)

| База | Коллекции | Назначение |
|------|-----------|-----------|
| diplom_shop | users, products, orders, user_test_participation | E-commerce данные |
| diplom_tests | ab_tests, test_archives, ab_rules | A/B тесты и правила |
| diplom_demographics | user_demographics | Демографические профили |
