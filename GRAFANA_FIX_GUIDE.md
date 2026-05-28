# 📊 Исправление Grafana: "No Data" проблема

## ❌ Проблема

В Grafana отображаются только:
- ✅ CPU потребление
- ✅ Память

Остальные метрики: **"No Data"**

---

## 🔍 Корень проблемы

### 1️⃣ **Prometheus не скрейпил все сервисы**

**Было:**
```yaml
scrape_configs:
  - job_name: 'diplom-shop'
    targets: ['app:8080']  # ❌ ТОЛЬКО ОДИН!
```

**Исправлено:**
```yaml
scrape_configs:
  - job_name: 'diplom-shop'
    targets: ['shop:8080']
  - job_name: 'diplom-test-service'
    targets: ['test:8081']
  - job_name: 'diplom-selector-service'
    targets: ['selector:8082']
  - job_name: 'diplom-demographic-service'
    targets: ['demographic:8084']
  - job_name: 'diplom-notification-service'
    targets: ['notification:8083']
  - job_name: 'diplom-clustering-service'
    targets: ['clustering:8085']
```

**Файлы обновлены:**
- ✅ `diplom/monitoring/prometheus.yml`
- ✅ `diplom-notification-service/monitoring/prometheus.yml`
- ✅ `monitoring/prometheus.yml`

---

### 2️⃣ **Java сервисы не полностью включали метрики**

**Было:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,prometheus
```

**Исправлено:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,prometheus
      base-path: /actuator  # ❌ БЫЛО ПРОПУЩЕНО
  endpoint:
    health:
      show-details: never    # ❌ БЫЛО ПРОПУЩЕНО
    prometheus:
      enabled: true          # ❌ БЫЛО ПРОПУЩЕНО
  metrics:
    tags:
      application: diplom-shop  # ❌ БЫЛО ПРОПУЩЕНО
```

**Файлы обновлены:**
- ✅ `diplom-test-service/src/main/resources/application.yml`
- ✅ `diplom-selector-service/src/main/resources/application.yml`
- ✅ `diplom-demographic-service/src/main/resources/application.yml`
- ✅ `diplom-notification-service/src/main/resources/application.yml`

---

## 🔧 Что нужно сделать

### Шаг 1: Пересобрать сервисы

```bash
cd diplom-test-service
mvn clean package -DskipTests

cd diplom-selector-service
mvn clean package -DskipTests

cd diplom-demographic-service
mvn clean package -DskipTests

cd diplom-notification-service
mvn clean package -DskipTests
```

### Шаг 2: Пересоздать контейнеры

```bash
docker-compose down
docker-compose up -d
```

### Шаг 3: Дождаться инициализации (2-3 минуты)

```bash
# Проверить, что все сервисы запущены:
docker-compose ps

# Проверить, что Prometheus скрейпит все endpoints:
curl http://localhost:9090/api/v1/targets
```

### Шаг 4: Проверить метрики в Prometheus

Откройте: http://localhost:9090

Выполните PromQL запросы:
```
# Должны вернуть данные:
http_requests_total
jvm_memory_used_bytes
jvm_threads_count
kafka_consumer_lag
mongodb_driver_pool_checkedout_total
```

### Шаг 5: Обновить Grafana

Откройте: http://localhost:3000

Перейти в: **Grafana → Dashboards → A/B Testing**

Обновить страницу (F5) → должны появиться данные

---

## 📊 Какие метрики должны появиться

| Метрика | От сервиса | Примеры |
|---------|-----------|---------|
| **HTTP** | Все сервисы | RPS, Latency (P95/P99), Error Rate |
| **JVM** | Все Java сервисы | Heap/Non-heap Memory, GC Pauses, Thread Count |
| **Kafka** | selector, shop, test, notification | Consumer Lag, Records/sec |
| **MongoDB** | shop, test, demographic, notification | Connection Pool, Query Latency |
| **Process** | Все сервисы | CPU %, File Descriptors, Start Time |

---

## ✅ Проверка

### 1. Prometheus видит все endpoints

```bash
curl http://localhost:9090/api/v1/targets | jq '.data.activeTargets[] | {job: .labels.job, state: .health}'
```

**Должен вернуть все 6 сервисов с `state: "up"`**

### 2. Метрики собираются

```bash
curl http://localhost:8080/actuator/prometheus | head -20
curl http://localhost:8081/actuator/prometheus | head -20
curl http://localhost:8082/actuator/prometheus | head -20
curl http://localhost:8083/actuator/prometheus | head -20
curl http://localhost:8084/actuator/prometheus | head -20
curl http://localhost:8085/metrics | head -20  # Python FastAPI
```

**Должны вернуть метрики в Prometheus формате**

### 3. Grafana видит данные

В Grafana откройте любой график и видите данные вместо "No Data"

---

## 🚀 Если всё ещё не работает

### Проверить логи Prometheus

```bash
docker logs diplom-prometheus
# Ищите ошибки: "connection refused", "no such host"
```

### Проверить логи сервисов

```bash
docker logs diplom-shop
docker logs diplom-test-service
docker logs diplom-selector-service
```

**Ищите ошибки при инициализации**

### Проверить firewall/networking

```bash
# Тест внутренней сети Docker
docker exec diplom-prometheus \
  curl -v http://shop:8080/actuator/prometheus
```

### Полная перестройка (nuclear option)

```bash
docker-compose down -v
docker system prune -a --volumes
docker-compose build --no-cache
docker-compose up -d
```

---

## 📝 Итоговый чек-лист

- [ ] Обновлены все `prometheus.yml` (3 файла)
- [ ] Обновлены все `application.yml` (5 сервисов)
- [ ] Сервисы пересобраны (`mvn clean package`)
- [ ] Контейнеры пересозданы (`docker-compose down && up`)
- [ ] Prometheus показывает все 6 сервисов как "up"
- [ ] Prometheus собирает метрики (curl /actuator/prometheus)
- [ ] Grafana отображает данные вместо "No Data"

---

## 💡 Почему это произошло?

1. **Prometheus конфиг был неполный** — имел только diplom-shop
2. **Java сервисы не экспортировали метрики полностью** — отсутствовал base-path
3. **Grafana дашборд искал правильные метрики, но их не было** → "No Data"

Теперь все 6 сервисов экспортируют полный набор метрик:
- ☕ JVM metrics
- 🌐 HTTP metrics
- 📬 Kafka metrics
- 🗄️ MongoDB metrics
- 📈 Process metrics
