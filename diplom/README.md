# DiplomShop — A/B Testing E-Commerce Platform

Учебный проект: интернет-магазин с встроенной системой A/B тестирования, аналитикой, персонализированными уведомлениями и AI-ботом для управления тестами через Telegram.

---

## Идея проекта

Платформа позволяет запускать A/B тесты для пользователей магазина: в зависимости от демографических данных (возраст, страна, пол) и правил маршрутизации каждый пользователь видит определённый вариант дизайна страницы. Результаты тестов анализируются через метрики поведения (просмотры, клики, глубина скролла). Тесты можно создавать голосом или текстом через Telegram-бот с помощью AI.

---

## Сервисы

| Сервис | Порт | Назначение |
|---|---|---|
| **diplom-shop** | 8080 | Основное Spring Boot приложение: магазин, Thymeleaf UI, метрики, A/B маршрутизация |
| **diplom-test-service** | 8081 | Управление A/B конфигами и правилами, сохранение участников и назначений вариантов |
| **diplom-selector-service** | 8082 | Kafka Streams — распределяет пользователей по тестам на основе демографических данных |
| **diplom-notification-service** | 8083 | Email рассылки (Gmail SMTP + STARTTLS) и Telegram-уведомления для пользователей и администраторов |
| **diplom-demographic-service** | 8084 | Хранит и отдаёт демографические профили пользователей (возраст, страна, пол, язык) |
| **n8n** | 5678 | Low-code автоматизация: Telegram-бот с AI (Groq LLama + Whisper) для создания тестов |
| **MongoDB** | 27017 | Основная БД (несколько баз: shop, tests, notifications, demographics) |
| **Apache Kafka** (KRaft) | 9092/9094 | Шина событий: пользовательские события → обработка → назначение в тесты |
| **MinIO** | 9000/9001 | S3-совместимое хранилище для изображений товаров |

---

## Workflow

```
      Пользователь открывает страницу
              │
              ▼
    ┌─────────────────────┐
    │   diplom-shop       │  ABInterceptor определяет тест и вариант
    │   (port 8080)       │  для текущего пользователя
    └────────┬────────────┘
             │  HTTP GET /api/ab/resolve?userId=&path=
             ▼
    ┌─────────────────────┐
    │  diplom-test-service│  Проверяет правила (ABRule → ABConfig),
    │  (port 8081)        │  назначает вариант (sticky, weighted random)
    └────────┬────────────┘
             │ возвращает { abTestId, variant }
             ▼
    diplom-shop рендерит шаблон: templates/{abTestId}/{variant}/page.html
             │
             │ JS фиксирует события (PAGE_VIEW, CLICK, SCROLL)
             ▼
    POST /api/metrics/event  →  MongoDB metrics collection


      Регистрация нового пользователя
              │  Kafka topic: user-registered
              ▼
    ┌─────────────────────────┐
    │ diplom-selector-service │  Читает демографику из demographic-service,
    │ (port 8082)             │  сопоставляет с критериями тестов,
    └────────┬────────────────┘  публикует: user-assigned-to-test
             │
             ▼
    diplom-test-service сохраняет участника теста (TestParticipant)


      Администратор создаёт рассылку
              │
              ▼
    ┌──────────────────────────────┐
    │ diplom-notification-service  │  Отправляет Email (Gmail STARTTLS)
    │ (port 8083)                  │  или Telegram-сообщение пользователям
    └──────────────────────────────┘
              │
              ▼
    Telegram Admin Bot (polling) — уведомляет администратора о событиях


      Telegram-бот (n8n, port 5678)
              │
              │  /start, /help → справочное сообщение
              │  текст/голос   → Groq LLama-3.3 парсит описание теста
              ▼
    HTTP POST → diplom-test-service /api/tests  →  тест создан
```

---

## Tech Stack

| Layer          | Technology                                                   |
|----------------|--------------------------------------------------------------|
| Language       | Java 21                                                      |
| Framework      | Spring Boot 3.2                                              |
| Database       | MongoDB (multi-database)                                     |
| Message Bus    | Apache Kafka (KRaft, no Zookeeper)                           |
| Image Storage  | MinIO (S3-compatible)                                        |
| Frontend       | Thymeleaf + custom CSS                                       |
| Security       | Spring Security, BCrypt, Cookie CSRF                         |
| AI / Bot       | n8n + Groq (LLama-3.3-70b + Whisper-large-v3)               |
| Build          | Maven, Docker, Docker Compose                                |

---

## Project Structure

```
src/main/java/com/diplom/
├── config/          # Security, S3, MongoDB, DataInitializer
├── controller/      # Web (Thymeleaf) + REST (metrics placeholder)
├── dto/             # Validation-annotated input DTOs
├── model/           # MongoDB @Document entities
├── repository/      # Spring Data MongoDB repositories
├── security/        # MongoUserDetailsService
└── service/         # Business logic

src/main/resources/
├── application.yml
├── static/
│   ├── css/styles.css   # shared + A/B variant styles
│   └── js/main.js       # metrics placeholder
└── templates/
    ├── fragments/       # shared Thymeleaf fragments (navbar)
    ├── auth/            # login, register
    ├── default/         # home — no active A/B test
    ├── variant-a/       # home — variant A (bold, dark, visual-first)
    ├── variant-b/       # home — variant B (minimal, list-based)
    ├── products/        # list, detail
    ├── profile/         # view, edit
    └── admin/           # products, ab-tests, ab-test-participants, users
```

---

## Domain Model

### User
| Field     | Type    | Notes                             |
|-----------|---------|-----------------------------------|
| login     | String  | unique, 3-50 chars, alphanumeric  |
| password  | String  | BCrypt encoded                    |
| firstName | String  |                                   |
| lastName  | String  |                                   |
| country   | String  |                                   |
| language  | String  |                                   |
| gender    | Enum    | MALE / FEMALE / OTHER             |
| age       | Integer |                                   |
| phone     | String  | optional, E.164-ish               |
| email     | String  | unique                            |

### Product
| Field             | Type       |
|-------------------|------------|
| name              | String     |
| price             | BigDecimal |
| description       | String     |
| photoKey          | String     | S3 key |
| photoUrl          | String     | public URL |
| availableQuantity | Integer    |

### UserTestParticipation
| Field      | Type   | Notes                     |
|------------|--------|---------------------------|
| testId     | String | ref to ABTest             |
| userId     | String | ref to User               |
| variant    | String | "A" or "B"                |
| enrolledAt | LDT    |                           |

Compound unique index on `(testId, userId)` prevents double-enrollment.

---

## A/B Testing Flow

```
User visits /
     │
     ▼
FrontendController checks ABTestService.findActiveParticipation(userId)
     │
     ├─── participation found ──► variant = "A" → render variant-a/home.html
     │                        └─► variant = "B" → render variant-b/home.html
     │
     └─── no participation ──────────────────────► render default/home.html
```

**Variant A** — Dark theme, card grid, big hero, orange accent color.  
**Variant B** — Minimal white, product list with all info inline.  
**Default**   — Standard blue/white Bootstrap-style card grid.

---

## Metrics Extension Points

Three placeholder entry points are ready for metrics implementation:

1. **`UserEvent` model** — broad schema: `eventType`, `eventData (Map)`, `page`, `testId`, `variant`, `sessionId`, `userAgent`, `ipAddress`.
2. **`MetricsService`** — `recordEvent()` logs and discards until persistence is uncommented.
3. **`MetricsController`** — REST endpoints (`POST /api/metrics/event`, `GET /api/metrics/test/{id}`, `GET /api/metrics/user/{id}`) return 501 until implemented.
4. **`main.js`** — contains a commented-out `trackEvent()` template ready for front-end events.

To enable metrics: uncomment `userEventRepository.save(event)` in `MetricsService` and implement the aggregation endpoints.

---

## Quick Start

### Prerequisites

- Java 21
- Maven 3.9+
- MongoDB (local or Docker)
- MinIO or AWS S3 bucket

### Run with Docker Compose (MongoDB + MinIO)

```yaml
# docker-compose.yml example (not included — add to project root)
services:
  mongo:
    image: mongo:7
    ports: ["27017:27017"]
  minio:
    image: minio/minio
    ports: ["9000:9000", "9001:9001"]
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin
    command: server /data --console-address ":9001"
```

### Build & Run

```bash
# Copy and fill in env vars
cp .env.example .env

# Build
mvn clean package -DskipTests

# Run (set env vars or edit application.yml)
java -jar target/diplom-shop-0.0.1-SNAPSHOT.jar
```

The app starts on **http://localhost:8080**.

### Default Admin Account

On first startup a default admin is created:
- **Login:** `admin`
- **Password:** `Admin1234!`

> **Change this password immediately after first login.**

---

## Admin Panel

| URL                                | Description          |
|------------------------------------|----------------------|
| `/admin/products`                  | Create / delete products |
| `/admin/ab-tests`                  | Create tests, enroll users |
| `/admin/ab-tests/{id}/participants`| Manage test participants |
| `/admin/users`                     | Browse all users     |

---

## API

| Method | URL                        | Description              |
|--------|----------------------------|--------------------------|
| POST   | `/api/metrics/event`       | Record user event (501)  |
| GET    | `/api/metrics/test/{id}`   | Test metrics (501)       |
| GET    | `/api/metrics/user/{id}`   | User metrics (501)       |
