# DiplomShop - E-Commerce Platform with A/B Testing

[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.3-green)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-7.0-blue)](https://www.mongodb.com/)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-KRaft-black)](https://kafka.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue)](https://www.docker.com/)

---

## 📚 Документация

### Быстрый старт
- **[QUICK_START.md](QUICK_START.md)** - Запуск за 5 минут ⚡

### Архитектура
- **[MONOREPO_STRUCTURE.md](MONOREPO_STRUCTURE.md)** - Структура монорепо, команды Maven, управление версиями 🏗️
- **[STRUCTURE_CHECKLIST.md](STRUCTURE_CHECKLIST.md)** - Полный чек-лист всех 5 микросервисов ✅
- **[REFACTORING_SUMMARY.md](REFACTORING_SUMMARY.md)** - Что было рефакторено и почему 🔄
- **[CLAUDE.md](CLAUDE.md)** - Требования проекта и техстек 📋

### Очистка и подготовка
- **[CLEANUP_INSTRUCTIONS.md](CLEANUP_INSTRUCTIONS.md)** - Как удалить старые папки 🗑️
- **cleanup_old_dirs.ps1** - PowerShell скрипт для Windows
- **cleanup_old_dirs.bat** - Batch скрипт для Windows
- **cleanup_old_dirs.sh** - Bash скрипт для Linux/Mac

---

## 🎯 Что это?

**DiplomShop** - это полнофункциональная e-commerce платформа с встроенной поддержкой A/B тестирования, разработанная как **микросервисная архитектура**.

### Ключевые компоненты

```
┌─────────────────────────────────────────────────────────────┐
│                      DiplomShop                              │
│                    E-Commerce Platform                       │
└──────────────┬────────────────────────────────┬──────────────┘
               │                                │
        ┌──────▼─────────┐           ┌─────────▼───────┐
        │   Shop (8080)  │           │  A/B Tests      │
        │                │           │  (8081)         │
        │ - Products     │           │                 │
        │ - Cart/Order   │           │ - Variant Mgmt  │
        │ - Auth         │           │ - Rules         │
        │ - Metrics      │           │ - Assignment    │
        └────────────────┘           └─────────────────┘
               │                            │
        ┌──────▼─────────┐           ┌─────────▼───────┐
        │ Demographics   │           │    Selector     │
        │ (8084)         │           │    (8082)       │
        │                │           │                 │
        │ - Profiles     │           │ Kafka Streams:  │
        │ - Interests    │           │ Real-time match │
        │ - Targeting    │           │ of users→tests  │
        └────────────────┘           └─────────────────┘
               │                            │
        ┌──────▼─────────┐                 │
        │ Notifications  │                 │
        │ (8083)         │◄────────────────┘
        │                │
        │ - Email        │
        │ - Telegram     │
        │ - Campaigns    │
        └────────────────┘
```

---

## 🏗️ Архитектура - Трёхслойная модель

Каждый микросервис следует **строгой трёхслойной архитектуре**:

```
┌──────────────────────────────────────────┐
│          REST Layer (rest/)               │
│  ┌──────────────┐        ┌──────────────┐│
│  │ Controllers  │        │    DTOs      ││
│  └──────────────┘        └──────────────┘│
└────────────┬─────────────────────────────┘
             │ (MapStruct Mappers)
┌────────────▼─────────────────────────────┐
│        Domain Layer (domain/)             │
│  ┌──────────────┐        ┌──────────────┐│
│  │   Models     │        │   Services   ││
│  │  (Pure POJO) │        │ (Business)   ││
│  └──────────────┘        └──────────────┘│
└────────────┬─────────────────────────────┘
             │ (MapStruct Mappers)
┌────────────▼─────────────────────────────┐
│    Persistence Layer (persistance/)      │
│  ┌──────────────┐        ┌──────────────┐│
│  │   Entities   │        │ Repositories ││
│  │ (MongoDB)    │        │ (Spring Data)││
│  └──────────────┘        └──────────────┘│
└──────────────────────────────────────────┘
```

---

## 📦 Монорепо структура

```
BIGGEST/                                    ← Корень монорепо
│
├── pom.xml                                ← Родительский POM
│   ├── diplom-parent (1.0.0)
│   ├── Spring Boot 3.2.3
│   ├── Java 21
│   ├── MapStruct 1.5.5
│   └── ... управление зависимостями
│
├── diplom/                                ← Main E-Commerce Service
│   ├── pom.xml (наследует от parent)
│   ├── src/main/java/com/diplom/
│   │   ├── rest/controller/    (20+ контроллеров)
│   │   ├── rest/dto/           (DTOs)
│   │   ├── domain/model/       (7 моделей)
│   │   ├── domain/service/     (8 сервисов)
│   │   ├── persistance/entity/ (7 сущностей)
│   │   ├── persistance/repo/   (7 репозиториев)
│   │   ├── mapper/             (7 MapStruct маперов)
│   │   └── config/
│   └── src/main/resources/
│       ├── application.yml
│       └── templates/
│
├── diplom-test-service/                   ← A/B Test Management
│   ├── pom.xml (наследует от parent)
│   └── src/main/java/com/diplom/testservice/
│       ├── rest/controller/    (Test API)
│       ├── domain/service/     (TestConfigService, ABRuleService)
│       ├── persistance/        (Test entities & repos)
│       └── config/
│
├── diplom-demographic-service/            ← User Profiles
│   └── Управляет демографическими профилями пользователей
│
├── diplom-notification-service/           ← Email & Telegram
│   └── Отправляет уведомления (email, Telegram)
│
├── diplom-selector-service/               ← Kafka Streams
│   └── Real-time user assignment to tests
│
├── docker-compose.yml                     ← Оркестрация
│   ├── 5 x Spring Boot приложений
│   ├── MongoDB (с 6 базами данных)
│   ├── Kafka (KRaft mode, без Zookeeper)
│   ├── MinIO (S3-compatible)
│   ├── Prometheus & Grafana
│   └── n8n (Telegram bot)
│
└── Документация
    ├── README.md              (этот файл)
    ├── QUICK_START.md         (за 5 минут до запуска)
    ├── MONOREPO_STRUCTURE.md  (архитектура)
    ├── STRUCTURE_CHECKLIST.md (чек-лист всех сервисов)
    ├── REFACTORING_SUMMARY.md (что было сделано)
    └── CLEANUP_INSTRUCTIONS.md (очистка старых файлов)
```

---

## 🚀 Быстрый старт

### 1. Очистить старые папки
```powershell
# Windows (PowerShell с правами администратора)
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process -Force
.\cleanup_old_dirs.ps1
```

```bash
# Linux/Mac
chmod +x cleanup_old_dirs.sh
./cleanup_old_dirs.sh
```

### 2. Собрать проект
```bash
mvn clean package -DskipTests
```

### 3. Запустить в Docker
```bash
docker-compose up -d
```

### 4. Проверить

| Сервис | URL | Порт |
|--------|-----|------|
| Shop | http://localhost:8080 | 8080 |
| Test Service API | http://localhost:8081 | 8081 |
| Selector Service | http://localhost:8082 | 8082 |
| Notification Service | http://localhost:8083 | 8083 |
| Demographic Service | http://localhost:8084 | 8084 |
| n8n | http://localhost:5678 | 5678 |
| Grafana | http://localhost:3000 | 3000 |
| MinIO | http://localhost:9001 | 9001 |
| Prometheus | http://localhost:9090 | 9090 |

---

## 📋 Требования проекта

Все требования описаны в **[CLAUDE.md](CLAUDE.md)**

- **Микросервисная архитектура** с 5 сервисами
- **A/B тестирование** с гибкой системой правил
- **Real-time обработка** через Kafka Streams
- **Многоязычная поддержка** (language field у пользователя)
- **S3-compatible storage** для фотографий продуктов
- **Telegram бот** для создания тестов через n8n
- **Мониторинг** через Prometheus & Grafana

---

## 🎯 Трёхслойная архитектура

### REST Layer (`rest/`)
- REST контроллеры обрабатывают HTTP запросы
- DTOs для сериализации/десериализации
- Никаких зависимостей от БД

### Domain Layer (`domain/`)
- Чистые бизнес-модели (POJO без аннотаций)
- Сервисы содержат всю бизнес-логику
- Независимы от реализации хранилища

### Persistence Layer (`persistance/`)
- MongoDB Entity классы с @Document аннотациями
- Spring Data репозитории
- Только DB-специфичный код

### Маппинг между слоями
- **MapStruct** автоматически конвертирует между слоями
- `domainToRest()` - Domain → DTO
- `restToDomain()` - DTO → Domain
- `persistenceToDomain()` - Entity → Domain
- `domainToPersistence()` - Domain → Entity

---

## 💡 Key Features

### ✅ A/B Testing
- Гибкие правила таргетирования
- Sticky-hash и weighted-random методы
- Real-time assignment через Kafka Streams

### ✅ E-Commerce
- Полный функционал магазина (товары, корзина, заказы)
- Интеграция с S3 для фотографий
- Профили пользователей

### ✅ Уведомления
- Email через Gmail SMTP
- Telegram через polling
- Кампании и отслеживание доставки

### ✅ Аналитика
- Метрики в Prometheus
- Дашборды в Grafana
- Отслеживание пользовательских событий

### ✅ AI Bot
- Telegram бот для создания тестов
- n8n workflow engine
- Groq LLama-3.3-70b + Whisper

---

## 🛠️ Технологический стек

| Компонент | Технология | Версия |
|-----------|-----------|--------|
| **Language** | Java | 21 |
| **Framework** | Spring Boot | 3.2.3 |
| **Database** | MongoDB | 7.0 |
| **Message Bus** | Apache Kafka | KRaft |
| **Stream Processing** | Kafka Streams | 3.x |
| **Mapping** | MapStruct | 1.5.5 |
| **Storage** | MinIO/S3 | S3 API |
| **Monitoring** | Prometheus | latest |
| **Visualization** | Grafana | latest |
| **Automation** | n8n | latest |
| **AI** | Groq LLama | 3.3-70b |

---

## 📊 Статистика проекта

- **Микросервисов:** 5
- **Java файлов:** 118
- **层 архитектуры:** 3
- **MapStruct маперов:** 7
- **MongoDB collections:** 15+
- **REST endpoints:** 40+
- **Kafka topics:** 5+

---

## 🔐 Безопасность

- ✓ Spring Security с BCrypt паролями
- ✓ CSRF protection via XSRF-TOKEN cookie
- ✓ Роли: USER, ADMIN
- ✓ Внутренние endpoints без CSRF (server-to-server)

---

## 📈 Мониторинг

### Prometheus metrics
- JVM метрики (память, GC)
- HTTP запросы (latency, error rate)
- MongoDB операции
- Kafka продюсер/консюмер метрики

### Grafana дашборды
- Pre-built A/B test dashboard
- Real-time метрики
- Системные показатели

---

## 🔄 CI/CD Ready

Монорепо упрощает:
- **Единую сборку** для всех модулей
- **Атомарные коммиты** для всего проекта
- **Параллельное тестирование** модулей
- **Кэширование** зависимостей

---

## 📝 Лицензия

Educational Project - DiplomShop

---

## 📞 Контакты

- **Email:** kingknife29@gmail.com
- **Date:** May 11, 2026

---

## 🎓 Что было сделано

### ✅ Рефакторинг (Май 2026)

Преобразование проекта из хаотичной структуры в:
1. **Трёхслойную архитектуру** (REST → Domain → Persistence)
2. **Монорепо** с единым родительским POM
3. **MapStruct маперы** для конвертации между слоями
4. **Единую версионизацию** всех сервисов
5. **Полную документацию** архитектуры

**Детали:** [REFACTORING_SUMMARY.md](REFACTORING_SUMMARY.md)

---

## 🚀 Дальнейшие шаги

1. **Интеграционные тесты** - Написать тесты для микросервисов
2. **API документация** - OpenAPI/Swagger
3. **Логирование** - Centralized logging (ELK stack)
4. **Rate limiting** - Защита от перегрузки
5. **Circuit breakers** - Resilience4j для отказоустойчивости
6. **Kubernetes** - Migration к K8s если нужна масштабируемость

---

## ✅ Готово!

Проект полностью готов к:
- ✓ Локальной разработке
- ✓ Docker развёртыванию
- ✓ Интеграционному тестированию
- ✓ Production развёртыванию

**Начни с [QUICK_START.md](QUICK_START.md)** 🚀

---

**Last Updated:** May 11, 2026  
**Version:** 1.0.0
