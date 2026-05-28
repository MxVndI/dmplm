# DiplomShop Architecture Diagrams

## 1. Общая архитектура системы (High-Level)

```mermaid
graph TB
    Client["🌐 Web Client<br/>React + Vite"]
    Admin["👨‍💼 Admin Panel"]
    
    subgraph "API Gateway & Load Balancing"
        Nginx["🔀 Nginx Reverse Proxy<br/>Port 80/443"]
    end
    
    subgraph "Microservices Tier"
        Shop["🛍️ diplom-shop<br/>Port 8080<br/>E-commerce Core"]
        Test["🧪 diplom-test-service<br/>Port 8081<br/>A/B Management"]
        Selector["📊 diplom-selector-service<br/>Port 8082<br/>Kafka Streams"]
        Demographic["👤 diplom-demographic-service<br/>Port 8084<br/>User Profiles"]
        Notification["📧 diplom-notification-service<br/>Port 8083<br/>Email/Telegram"]
        Clustering["🤖 diplom-clustering-service<br/>Port 8085<br/>Python ML K-means"]
    end
    
    subgraph "Data & Message Tier"
        MongoDB["🗄️ MongoDB 7.0<br/>6 Databases<br/>diplom_shop, diplom_tests,<br/>diplom_demographics, etc."]
        Kafka["📬 Apache Kafka<br/>KRaft Mode<br/>5 Topics"]
        Redis["💾 Redis Cache<br/>Session Storage"]
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
    
    Client -->|HTTP/REST| Nginx
    Admin -->|HTTP/REST| Nginx
    
    Nginx -->|Routes| Shop
    Nginx -->|Routes| Test
    Nginx -->|Routes| Selector
    Nginx -->|Routes| Demographic
    Nginx -->|Routes| Notification
    Nginx -->|Routes| Clustering
    
    Shop -->|REST API| Test
    Shop -->|REST API| Demographic
    Shop -->|Events| Kafka
    
    Test -->|REST API| Demographic
    Test -->|Events| Kafka
    Test -->|REST API| Clustering
    
    Selector -->|REST API| Demographic
    Selector -->|Consume| Kafka
    Selector -->|Produce| Kafka
    
    Notification -->|REST API| Shop
    Notification -->|Consume| Kafka
    
    Shop -->|Read/Write| MongoDB
    Test -->|Read/Write| MongoDB
    Demographic -->|Read/Write| MongoDB
    Notification -->|Read/Write| MongoDB
    
    Selector -->|State Store| MongoDB
    Clustering -->|Load| MongoDB
    
    Shop -->|Upload/Download| MinIO
    
    Notification -->|Send| Gmail
    Notification -->|Send| Telegram
    
    Shop -->|Metrics| Prometheus
    Test -->|Metrics| Prometheus
    Selector -->|Metrics| Prometheus
    Demographic -->|Metrics| Prometheus
    Notification -->|Metrics| Prometheus
    Clustering -->|Metrics| Prometheus
    
    Prometheus -->|Data| Grafana
    
    n8n -->|Trigger Workflows| Test
    n8n -->|Monitor| Prometheus

    style Nginx fill:#ff9900
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
    subgraph "Docker Compose Network"
        subgraph "Databases"
            db1["🗄️ MongoDB<br/>diplom-shop"]
            db2["🗄️ MongoDB<br/>diplom-tests"]
            db3["🗄️ MongoDB<br/>diplom-demographics"]
        end
        
        subgraph "Message Bus"
            kafka["📬 Kafka Broker<br/>KRaft Controller"]
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
            n8n_svc["⚙️ n8n:5678<br/>Workflows"]
        end
        
        subgraph "Storage"
            minio["🗂️ MinIO:9001<br/>S3 Compatible"]
        end
    end
    
    db1 -.-> shop
    db2 -.-> test
    db3 -.-> demographic
    
    kafka -.-> shop
    kafka -.-> selector
    kafka -.-> test
    kafka -.-> notification
    
    shop -.-> test
    shop -.-> demographic
    test -.-> demographic
    
    selector -.-> demographic
    
    test -.-> clustering
    
    shop -.-> minio
    
    shop -.-> prom
    test -.-> prom
    selector -.-> prom
    demographic -.-> prom
    notification -.-> prom
    clustering -.-> prom
    
    prom -.-> grafana
    
    n8n_svc -.-> test
    
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
graph TB
    Client["👤 Client Request"]
    
    Client -->|"GET /products"| Shop
    Client -->|"GET /tests/{id}/resolve?userId=X"| Test
    
    subgraph "Synchronous REST Calls"
        Shop["🛍️ diplom-shop<br/>ABInterceptor"]
        Test["🧪 diplom-test-service<br/>TestController"]
        Demographic["👤 diplom-demographic-service"]
        Clustering["🤖 diplom-clustering-service"]
    end
    
    Shop -->|"GET /api/profiles/{userId}<br/>HTTP + JWT"| Demographic
    Shop -->|"GET /api/tests/{id}/resolve"| Test
    
    Test -->|"1️⃣ GET /api/profiles/{userId}"| Demographic
    Test -->|"2️⃣ POST /api/cluster/assign<br/>{userId, features}"| Clustering
    
    Clustering -->|"Response: {clusterId, variant}"| Test
    Test -->|"Response: {testId, variant}"| Shop
    
    Shop -->|"Render variant template"| Client
    
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
        SelectorProd["📊 selector-service<br/>Publishes: user-segment-changes<br/>test-selection-requests"]
        TestProd["🧪 test-service<br/>Publishes: test-participants-result"]
    end
    
    subgraph "Kafka Broker (KRaft)"
        Topic1["📬 user-events<br/>3 partitions, 7d retention"]
        Topic2["📬 user-profiles<br/>Compacted"]
        Topic3["📬 user-segment-changes<br/>3 partitions"]
        Topic4["📬 test-selection-requests<br/>3 partitions"]
        Topic5["📬 test-participants-result<br/>3 partitions"]
    end
    
    subgraph "Event Consumers"
        SelectorCons["📊 selector-service<br/>Consumes: user-events<br/>user-profiles (GlobalKTable)<br/>Produces: segments"]
        TestCons["🧪 test-service<br/>Consumes: user-segment-changes<br/>test-selection-requests"]
        NotifCons["📧 notification-service<br/>Consumes: user-events<br/>test-participants-result<br/>Sends: Email/Telegram"]
        ShopCons["🛍️ diplom-shop<br/>Consumes: test-participants-result"]
    end
    
    ShopProd -->|"Publish"| Topic1
    DemoProd -->|"Publish"| Topic2
    SelectorProd -->|"Publish"| Topic3
    SelectorProd -->|"Publish"| Topic4
    TestProd -->|"Publish"| Topic5
    
    Topic1 -->|"Subscribe<br/>consumer_lag: 0-100"| SelectorCons
    Topic2 -->|"GlobalKTable<br/>In-memory cache"| SelectorCons
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

## 5. Полный Data Flow: От запроса до A/B варианта

```mermaid
graph TD
    Start["👤 User Request<br/>GET /?userId=user123&testId=test-checkout"]
    
    Start -->|"1. ABInterceptor intercepts"| Shop["🛍️ diplom-shop"]
    
    Shop -->|"2. REST: GET /api/tests/test-checkout/resolve?userId=user123"| Test["🧪 diplom-test-service<br/>ABTestService"]
    
    Test -->|"3. Check UserTestParticipation<br/>(sticky variant)"| TestDB[("💾 MongoDB<br/>diplom_tests")]
    
    TestDB -->|"Not found (new user)"| Test
    
    Test -->|"4. REST: GET /api/profiles/user123"| Demographic["👤 demographic-service"]
    
    Demographic -->|"Returns: age, country,<br/>gender, language"| DemoDB[("💾 MongoDB<br/>diplom_demographics")]
    
    DemoDB -->|"Profile data"| Demographic
    
    Demographic -->|"Profile data"| Test
    
    Test -->|"5. Evaluate ABRule<br/>(age >= 18, country=US)"| Decision{{"Does user<br/>match rule?"}}
    
    Decision -->|"No"| Reject["❌ Not eligible<br/>Return null"]
    
    Decision -->|"Yes"| Cluster["🤖 Call clustering-service"]
    
    Test -->|"6. REST: POST /api/cluster/assign<br/>{userId, features}"| Clustering["🤖 diplom-clustering-service<br/>Python FastAPI"]
    
    Clustering -->|"Load pretrained<br/>K-means model"| ClusterMem["💾 In-memory model<br/>n_clusters=4"]
    
    ClusterMem -->|"7. Normalize features:<br/>visitCount, purchaseCount,<br/>totalSpent, ..."| Normalize["⚙️ StandardScaler"]
    
    Normalize -->|"8. Find nearest centroid<br/>distance = euclidean"| KMeans["🎯 K-means assignment"]
    
    KMeans -->|"clusterId: 0-3"| ClusterResult{{"clusterId<br/>→ Variant?"}}
    
    ClusterResult -->|"Cluster 0,1 → A"| VariantA["📌 Variant A"]
    ClusterResult -->|"Cluster 2,3 → B"| VariantB["📌 Variant B"]
    
    VariantA -->|"Response: variant=A"| Test
    VariantB -->|"Response: variant=B"| Test
    
    Test -->|"9. Save to UserTestParticipation<br/>(testId, userId, variant, clusterId)"| TestDB
    
    Test -->|"10. Publish event: test-participants-result"| Kafka["📬 Kafka<br/>test-participants-result"]
    
    Kafka -->|"Async: Consume"| Selector["📊 selector-service<br/>Update internal state"]
    Kafka -->|"Async: Consume"| Notification["📧 notification-service<br/>Track enrollment"]
    
    Test -->|"11. Return to client"| Shop
    
    Shop -->|"12. Inject variant header"| Response["🎨 Render template<br/>templates/{testId}/{variant}/page.html"]
    
    Response -->|"Response to browser"| Client["✅ Variant-specific UI"]
    
    style Shop fill:#4CAF50
    style Test fill:#2196F3
    style Clustering fill:#FFC107
    style Demographic fill:#FF5722
    style Kafka fill:#000000,color:#fff
    style Selector fill:#9C27B0
    style Notification fill:#00BCD4
    style Client fill:#90EE90
```

---

## 6. REST API маршруты и Kafka топики

```mermaid
graph LR
    subgraph "REST API Endpoints"
        GET1["GET /api/products"]
        GET2["GET /api/products/{id}"]
        POST1["POST /api/tests"]
        GET3["GET /api/tests/{id}/resolve"]
        POST2["POST /api/cart"]
        GET4["GET /api/profiles/{userId}"]
        POST3["POST /api/cluster/assign"]
        POST4["POST /api/notifications/telegram"]
    end
    
    subgraph "Kafka Topics (Async)"
        K1["📬 user-events"]
        K2["📬 user-profiles"]
        K3["📬 user-segment-changes"]
        K4["📬 test-selection-requests"]
        K5["📬 test-participants-result"]
    end
    
    subgraph "Services"
        Shop["🛍️ shop"]
        Test["🧪 test-service"]
        Selector["📊 selector"]
        Demographic["👤 demographic"]
        Notification["📧 notification"]
        Clustering["🤖 clustering"]
    end
    
    GET1 --> Shop
    GET2 --> Shop
    POST2 --> Shop
    
    POST1 --> Test
    GET3 --> Test
    
    GET4 --> Demographic
    
    POST3 --> Clustering
    
    POST4 --> Notification
    
    Shop -.->|"Produces"| K1
    Demographic -.->|"Produces"| K2
    Selector -.->|"Produces"| K3
    Selector -.->|"Produces"| K4
    Test -.->|"Produces"| K5
    
    K1 -.->|"Consumes"| Selector
    K1 -.->|"Consumes"| Notification
    K2 -.->|"GlobalKTable"| Selector
    K3 -.->|"Consumes"| Test
    K4 -.->|"Consumes"| Test
    K5 -.->|"Consumes"| Notification
    K5 -.->|"Consumes"| Shop
    
    style K1 fill:#000000,color:#fff
    style K2 fill:#000000,color:#fff
    style K3 fill:#000000,color:#fff
    style K4 fill:#000000,color:#fff
    style K5 fill:#000000,color:#fff
```

---

## 7. Процесс завершения A/B теста и архивирования

```mermaid
graph TD
    Test["🧪 A/B Test ACTIVE<br/>expiresAt = now + 1h"]
    
    Timer["⏱️ Scheduler runs<br/>every 60 seconds"]
    
    Timer -->|"Check: now >= expiresAt?"| Check{{"Is test<br/>expired?"}}
    
    Check -->|"No"| Wait["⏳ Wait next cycle"]
    Check -->|"Yes"| Aggregate["📊 Aggregate metrics"]
    
    Aggregate -->|"Count participants:<br/>variant_a.count<br/>variant_b.count<br/>conversion_a<br/>conversion_b"| Calc["🧮 Calculate stats"]
    
    Calc -->|"Create TestArchiveEntity"| Archive["📁 Archive to test_archives<br/>{testId, name, status,<br/>duration, variant_a, variant_b,<br/>completedAt}"](("💾 MongoDB"))
    
    Archive -->|"Notify admin"| Telegram["💬 TelegramService<br/>notifyTestCompletion"]
    
    Telegram -->|"✅ Test 'Checkout Page' expired.<br/>Variant A: 245 users, 32 conversions (13%)<br/>Variant B: 255 users, 48 conversions (19%)"| TelegramAPI["🔔 Telegram Bot API"]
    
    Archive -->|"Clear participants"| Clear["🗑️ Delete from<br/>UserTestParticipation<br/>where testId=X"]
    
    Clear -->|"Set status=COMPLETED"| Complete["✅ Update ABTest<br/>status=COMPLETED"]
    
    Complete -->|"Publish event"| Kafka["📬 Kafka:<br/>test-completed"]
    
    Kafka -->|"Consume"| Notification["📧 notification-service"]
    
    Notification -->|"Send notification<br/>Email to owner"| Email["📨 Gmail SMTP"]
    
    Email -->|"✉️ Your test results"| Owner["👨‍💼 Test Owner"]
    
    style Test fill:#2196F3
    style Telegram fill:#00BCD4
    style Archive fill:#FF9800
    style Complete fill:#4CAF50
    style Owner fill:#90EE90
```

---

## 8. K-means кластеризация flow

```mermaid
graph LR
    User["👤 User<br/>userId=user123"]
    
    User -->|"POST /api/cluster/assign<br/>{userId, features: {...}}"| API["🔌 FastAPI<br>/api/cluster/assign"]
    
    API -->|"1. Extract 8 features"| Features["📊 Features vector<br/>visitCount7Days: 5<br/>purchaseCount: 2<br/>totalSpent: 150.5<br/>cartAddCount: 8<br/>productViewCount: 20<br/>cartAbandoned: 0<br/>daysSinceLastEvent: 2.5<br/>hoursSinceLastCart: 6.0"]
    
    Features -->|"2. Normalize"| Normalize["⚙️ StandardScaler<br/>mean=0, std=1"]
    
    Normalize -->|"3. Load model from disk<br/>or Redis cache"| Model["🧠 Pre-trained K-means<br/>n_clusters=4<br/>centroids loaded"]
    
    Model -->|"4. Compute distances<br/>to 4 centroids"| Distance["📏 Euclidean distance<br/>d_0 = 0.45<br/>d_1 = 0.82<br/>d_2 = 1.20<br/>d_3 = 2.10"]
    
    Distance -->|"5. Find min distance"| Assign["🎯 Assign to cluster 0<br/>(nearest centroid)"]
    
    Assign -->|"6. Map cluster → variant"| Mapping{{"Cluster 0,1<br/>→ Variant A<br/>---<br/>Cluster 2,3<br/>→ Variant B"}}
    
    Mapping -->|"clusterId=0"| VariantA["📌 Variant: A"]
    
    VariantA -->|"Response:<br/>{<br/>  clusterId: 0,<br/>  variant: 'A',<br/>  distance: 0.45<br/>}"| Response["✅ Return to test-service"]
    
    style API fill:#FFC107
    style Features fill:#FF9800
    style Model fill:#673AB7
    style Distance fill:#2196F3
    style VariantA fill:#4CAF50
    style Response fill:#90EE90
```

---

## 9. Мониторинг и Alerting

```mermaid
graph TB
    subgraph "Data Collection"
        JVM["☕ JVM Metrics<br/>Memory, GC, Threads"]
        HTTP["🌐 HTTP Metrics<br/>RPS, Latency, Errors"]
        Kafka["📬 Kafka Metrics<br/>Consumer Lag, Throughput"]
        MongoDB["🗄️ MongoDB Metrics<br/>Connections, Queries"]
    end
    
    subgraph "Prometheus"
        Scrape["📈 Prometheus Scraper<br/>Interval: 15s<br/>Retention: 7d"]
        TSDB["💾 Time Series DB<br/>Metrics storage"]
    end
    
    subgraph "Grafana"
        Dashboard1["📊 A/B Testing Dashboard<br/>Test count, participants,<br/>conversion rates"]
        Dashboard2["📊 System Health<br/>CPU, Memory, Connections"]
        Dashboard3["📊 API Performance<br/>RPS, P95 latency"]
    end
    
    subgraph "Alerting"
        Rules["⚠️ Alert Rules<br/>if P95_latency > 500ms<br/>if ErrorRate > 2%<br/>if ConsumerLag > 1000"]
        AlertMgr["🔔 Alert Manager"]
    end
    
    JVM --> Scrape
    HTTP --> Scrape
    Kafka --> Scrape
    MongoDB --> Scrape
    
    Scrape --> TSDB
    
    TSDB -->|"Query PromQL"| Dashboard1
    TSDB -->|"Query PromQL"| Dashboard2
    TSDB -->|"Query PromQL"| Dashboard3
    
    TSDB --> Rules
    Rules -->|"Fire if true"| AlertMgr
    
    AlertMgr -->|"Slack / PagerDuty"| OnCall["👨‍⚖️ On-call Engineer"]
    
    style Scrape fill:#E34234
    style TSDB fill:#E34234,color:#fff
    style Dashboard1 fill:#FF9830
    style Dashboard2 fill:#FF9830
    style Dashboard3 fill:#FF9830
    style AlertMgr fill:#FF5722
```

---

## 10. Deployment Pipeline (Docker Compose → Kubernetes)

```mermaid
graph LR
    Dev["💻 Developer<br/>git push"]
    
    Dev --> GitHub["🐙 GitHub Repo"]
    
    GitHub -->|"Webhook"| CI["🔧 CI Pipeline<br/>Maven: mvn clean package"]
    
    CI -->|"Build JAR"| Docker["🐳 Docker Build<br/>Multi-stage build<br/>maven:3.9 → temurin:21"]
    
    Docker -->|"Push image"| Registry["📦 Docker Registry<br/>ghcr.io/repo/service:latest"]
    
    Registry -->|"docker pull"| DevDeploy["📦 Local Dev<br/>docker-compose up"]
    
    Registry -->|"docker pull"| K8s["☸️ Kubernetes Cluster<br/>Helm charts"]
    
    DevDeploy -->|"Integration tests"| Tests["✅ Test Suite<br/>Testcontainers"]
    
    Tests -->|"Pass/Fail"| Result{{"Status?"}}
    
    Result -->|"Pass"| Prod["🚀 Production Deploy<br/>Rolling update<br/>Health checks"]
    
    Prod -->|"Traffic"| LB["⚖️ Load Balancer<br/>Auto-scale based on metrics"]
    
    style CI fill:#4CAF50
    style Docker fill:#2196F3
    style Registry fill:#FF9800
    style K8s fill:#673AB7
    style Prod fill:#4CAF50
```
