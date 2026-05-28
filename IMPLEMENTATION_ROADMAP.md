# DiplomShop A/B Testing Platform - Implementation Roadmap

## Система: Реал-тайм платформа для A/B тестирования в e-commerce

---

## АРХИТЕКТУРНЫЙ ОБЗОР

```
┌─────────────────────────────────────────────────────────────────┐
│                         ПОЛЬЗОВАТЕЛЬ                             │
└────────────────┬────────────────────────────────────────────────┘
                 │
        ┌────────▼──────────┐
        │  diplom-shop      │ (Frontend + API)
        │  :8080            │
        └────────┬──────────┘
                 │ ◄─── HTTP Запросы
    ┌────────────┼────────────────┐
    │            │                │
    ▼            ▼                ▼
┌─────────┐ ┌─────────┐ ┌──────────────┐
│Test-svc │ │Selector │ │Notification  │
│:8081    │ │:8082    │ │:8083         │
└────┬────┘ └────┬────┘ └──────┬───────┘
     │           │             │
     └───────────┼─────────────┘
                 │
        ┌────────▼──────────┐
        │  MongoDB          │
        │  (5 databases)    │
        └───────────────────┘

Kafka Topics:
  - user-registered → новые пользователи
  - user-profiles → демографические данные
  - test-config-updated → конфигурация тестов
  - user-assigned-to-test → распределение пользователей
  - user-events → клики, просмотры, скролл
  - notification-sent → отправленные уведомления
```

---

## 1. МИКРОСЕРВИС МАГАЗИНА (diplom-shop)

### 1.1 Сторона пользователя

#### Регистрация и аутентификация
```
POST /auth/register
- login (unique, min 3 chars)
- password (min 8 chars, BCrypt hash)
- email (optional but recommended)
- firstName, lastName
- age, country, language, gender
- phone (optional)
- telegramChatId (optional, может добавить позже)

Ответ:
- userId
- JWT token (если требуется)
- успешно/ошибка

POST /auth/login
- login
- password

GET /auth/logout
```

#### Профиль пользователя
```
GET /profile
- Просмотр текущих данных
- Демографические данные из diplom-demographic-service

PUT /profile
- Обновление firstName, lastName, phone, email
- Обновление telegramChatId

PUT /profile/demographics
- incomeLevel: LOW | MEDIUM | HIGH
- educationLevel: BASIC | SECONDARY | HIGHER | ACADEMIC
- occupation: STUDENT | EMPLOYED | SELF_EMPLOYED | UNEMPLOYED | RETIRED
- interests: ["tech", "sports", "travel", ...]

DELETE /profile
- Удаление аккаунта (мягкое удаление или полное?)
```

#### Каталог товаров
```
GET /products
- Поиск по названию (query param)
- Фильтрация по цене (min, max)
- Сортировка (price, name, newest)
- Пагинация (limit, offset)

Ответ:
- productId, name, description, price, photo, availableQuantity

GET /products/{id}
- Детальная информация о товаре
```

#### Корзина (SESSION-based)
```
POST /cart/add
- productId
- quantity

DELETE /cart/remove/{productId}

GET /cart
- Просмотр всех товаров в корзине
- Сумма товаров, итоговая стоимость

POST /cart/clear
- Очистить корзину
```

#### Оформление заказа
```
POST /checkout
- Автоматически берет товары из сессионной корзины
- Создает Order в БД
- Вычисляет A/B тест контекст (testId, variant)
- Сохраняет event "order_completed" в метрики
- Очищает корзину

Ответ:
- orderId
- Сумма
- Статус: COMPLETED
- testId и variant (если участвует в тесте)

GET /orders
- История покупок пользователя
- С фильтрацией по датам

GET /orders/{orderId}
- Детали конкретного заказа
```

#### Сбор метрик (client-side + server-side)
```
POST /metrics/events
- eventType: page_view | click | scroll | time_spent | order_completed
- eventData: JSON с дополнительной информацией
- page: URL страницы
- testId: (если участвует в тесте)
- variant: A/B/C/D (если участвует)
- sessionId: уникальный ID сессии
- userAgent, ipAddress

Метрики, которые сбираются:
- Page views (по страницам)
- Unique users (по IP/sessionId)
- Clicks (на какие элементы)
- Scroll depth (глубина прокрутки в %)
- Session duration (время на сайте в ms)
- Order completion rate (заказы / посещения)
- Order revenue (сумма заказов)
- Click-through rate на конкретные элементы
```

### 1.2 Сторона администратора

#### Управление пользователями
```
GET /admin/users
- Список всех пользователей
- Фильтр: по статусу (active/blocked), по дате регистрации
- Сортировка: по имени, по дате

GET /admin/users/{userId}
- Детали пользователя (профиль, история заказов, тесты)

POST /admin/users/{userId}/block
- Заблокировать пользователя
- Причина блокировки (опционально)

POST /admin/users/{userId}/unblock
- Разблокировать пользователя
```

#### Управление товарами
```
POST /admin/products
- name (обязательно)
- description
- price (decimal)
- availableQuantity (int)
- photo (file upload)
- category (опционально)

PUT /admin/products/{id}
- Изменение любых полей
- Смена фото

DELETE /admin/products/{id}
- Мягкое удаление (скрытие с сайта)

PUT /admin/products/{id}/stock
- Изменение количества товара
- availableQuantity: int

GET /admin/products
- Все товары с фильтрацией
- Включая скрытые (для админов)
```

#### A/B конфигурация (тесты)
```
POST /admin/ab-tests
- name: "Красные кнопки vs Синие кнопки"
- description: опционально
- variants: {
    "A": { label: "Red buttons", probability: 50 },
    "B": { label: "Blue buttons", probability: 50 }
  }
- targetPages: ["/products", "/checkout"]
- templateAUrl: "/templates/test1/A/products.html"
- templateBUrl: "/templates/test1/B/products.html"
- startDate, expiresAt

GET /admin/ab-tests
- Список активных и неактивных тестов

GET /admin/ab-tests/{testId}
- Детали теста + текущая статистика

PUT /admin/ab-tests/{testId}
- Изменение конфигурации (если не запущен)

POST /admin/ab-tests/{testId}/start
- Запуск теста
- Вызов селектора для распределения пользователей

POST /admin/ab-tests/{testId}/stop
- Остановка теста

POST /admin/ab-tests/{testId}/restart
- Перезапуск (очистка участников + заново запустить селектор)

DELETE /admin/ab-tests/{testId}
- Удаление теста

GET /admin/ab-tests/{testId}/metrics
- Метрики по тесту
```

#### Страницы/Шаблоны для тестов
```
POST /admin/templates
- testId (обязательно)
- variant: "A" | "B" | "C" | "D"
- name: "Дизайн карточки товара вариант A"
- htmlContent: (загрузить HTML файл или raw HTML)
- targetPage: "/products"

GET /admin/templates?testId={testId}
- Все шаблоны для теста

DELETE /admin/templates/{templateId}
```

---

## 2. МИКРОСЕРВИС КОНФИГУРАТОРА ТЕСТОВ (diplom-test-service)

### Основные компоненты

#### ABTest Entity
```
{
  id: String (UUID),
  name: String,
  description: String,
  variants: {
    "A": { probability: 50, rules: [...] },
    "B": { probability: 50, rules: [...] }
  },
  rules: [
    {
      id: String,
      priority: int,
      userId: String (optional - для specific user),
      pathPattern: "/products/**",
      active: boolean,
      conditions: {
        ageMin: 18,
        ageMax: 65,
        countries: ["Russia", "Ukraine"],
        languages: ["Russian", "Ukrainian"],
        genders: ["MALE", "FEMALE"]
      }
    }
  ],
  status: DRAFT | RUNNING | ACTIVE | COMPLETED,
  createdAt: DateTime,
  startedAt: DateTime (optional),
  expiresAt: DateTime (optional),
  endedAt: DateTime (optional)
}
```

#### Endpoints

```
POST /api/tests
- Создание конфигурации теста
- Требует ROLE_ADMIN
- Тело запроса: name, description, variants, rules, expiresAt

GET /api/tests
- Список всех тестов

GET /api/tests/{id}
- Детали теста

PUT /api/tests/{id}
- Редактирование (если DRAFT)

POST /api/tests/{id}/trigger-selection
- Запуск процесса распределения пользователей
- Публикует событие в Kafka для diplom-selector-service

POST /api/tests/{id}/activate
- Переход из RUNNING в ACTIVE

POST /api/tests/{id}/complete
- Завершение теста

DELETE /api/tests/{id}
- Удаление

GET /api/tests/{id}/resolve?userId={userId}&path={path}
- Для diplom-shop: определить, участвует ли пользователь в тесте и какой вариант ему показать
- Ответ: { testId: "...", variant: "A" } или 204 No Content
```

#### Интеграция с diplom-demographic-service
```
При создании теста с условиями по демографии:
- Сохраняются условия (educationLevel, incomeLevel и т.д.)
- При запуске селектора эти условия передаются
- Селектор использует API diplom-demographic-service для проверки
```

---

## 3. МИКРОСЕРВИС СЕЛЕКТОРА ПОЛЬЗОВАТЕЛЕЙ (diplom-selector-service)

### Архитектура: Kafka Streams

#### Входящие потоки (Topics)

```
1. user-profiles (GlobalKTable)
   - Key: userId
   - Value: { userId, age, country, language, gender }
   - Используется: для фильтрации пользователей по первичным демографам

2. test-selection-requests (KStream input)
   - Key: testId
   - Value: {
       testId: String,
       testName: String,
       rules: [...],
       criteria: {
         ageMin, ageMax,
         countries: [],
         languages: [],
         genders: [],
         incomeLevel: [],
         educationLevel: [],
         occupations: [],
         interests: []
       }
     }
   - Источник: diplom-test-service при POST /api/tests/{id}/trigger-selection
```

#### Обработка в Kafka Streams

```
Topology:
1. Читаем GlobalKTable user-profiles (память на каждом инстансе)
2. Читаем KStream test-selection-requests
3. Для каждого SelectionRequest:
   a) Перебираем ВСЕ пользователей из GlobalKTable
   b) Фильтруем по базовым критериям (age, country, language, gender)
   c) Если есть advanced criteria → вызываем diplom-demographic-service API
   d) Делим пользователей на группы по вероятностям вариантов
   e) Сохраняем TestParticipant в MongoDB (diplom_tests)
   f) Отправляем TestParticipantEvent в test-participants-result topic

Параллельно:
- Обновляем GlobalKTable когда приходят новые user-profiles
```

#### Entities и хранилище

```
TestParticipant {
  id: String (UUID),
  testId: String,
  userId: String,
  variant: "A" | "B" | "C" | "D",
  enrolledAt: DateTime
}

Collection: test_participants в diplom_tests базе
Индексы: (testId, userId) UNIQUE, testId для быстрого поиска
```

#### API для админа (опционально)

```
GET /api/selector/tests/{testId}/distribution
- Информация о распределении пользователей по группам
- Ответ: {
    total: 1500,
    distribution: {
      "A": 750,
      "B": 750
    },
    timestamp: "..."
  }
```

---

## 4. МИКРОСЕРВИС НОТИФИКАЦИЙ (diplom-notification-service)

### Email рассылки

#### SMTP конфигурация
```
application.yml:
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true
      mail.smtp.starttls.required: true
```

#### Endpoints

```
POST /api/campaigns
- Создание кампании рассылки
- name: "Уведомление про скидку вариант A"
- channel: EMAIL | TELEGRAM | BOTH
- subject: "Экклюзивная скидка для вас!"
- body: HTML контент письма
- targetType: ALL | SPECIFIC | TEST_VARIANT
- testId: (если TARGET_TYPE == TEST_VARIANT)
- testVariant: "A" (если TARGET_TYPE == TEST_VARIANT)
- recipientEmails: [...] (если SPECIFIC)

POST /api/campaigns/{id}/send
- Запуск рассылки
- Создает DeliveryRecord для каждого получателя
- Отправляет письма асинхронно

GET /api/campaigns
- Список кампаний

GET /api/campaigns/{id}/stats
- Статистика доставки:
  {
    total: 1000,
    delivered: 950,
    failed: 50,
    pending: 0
  }
```

#### Telegram рассылки

```
POST /api/campaigns (с channel: TELEGRAM)
- telegramChatIds: [123456789, ...] или testId для автоматического определения
- message: текст сообщения (без HTML, markdown поддерживается)
- sendAt: опционально, для отложенной отправки

Telegram Bot:
- API Token в переменной TELEGRAM_BOT_TOKEN
- Метод sendMessage для отправки
- Webhook или polling для получения сообщений от админов
```

#### Delivery tracking

```
DeliveryRecord {
  id: String,
  campaignId: String,
  userId: String,
  channel: EMAIL | TELEGRAM,
  recipient: email address или chat id,
  status: PENDING | DELIVERED | FAILED,
  errorMessage: String (if FAILED),
  sentAt: DateTime,
  deliveredAt: DateTime
}
```

---

## 5. ХРАНИЛИЩЕ ВЫБРАННЫХ ПОЛЬЗОВАТЕЛЕЙ

Это просто таблица в diplom-shop (основной базе):

```
TestParticipation {
  id: String (UUID),
  testId: String,
  userId: String,
  variant: "A" | "B" | "C" | "D",
  enrolledAt: DateTime
}

Collection: user_test_participations в diplom_shop базе
```

**Использование:**
- При запросе пользователя в diplom-shop проверяем есть ли он в тестировании
- Если да → возвращаем вариант → используем соответствующий шаблон/дизайн
- Если нет → показываем дефолтный вариант

---

## 6. ТЕЛЕГРАМ АДМИНКА (n8n)

### Основные команды

```
/start - приветствие, регистрация админа
/help - справка по командам

TEST MANAGEMENT:
/create_test - создание нового теста (интерактивное меню)
/list_tests - список активных тестов
/test_stats {testId} - статистика по тесту
/stop_test {testId} - остановка теста
/delete_test {testId} - удаление теста

PRODUCT MANAGEMENT:
/add_product - добавление товара (интерактивное)
/list_products - товары
/edit_product {productId} - редактирование

USER MANAGEMENT:
/list_users {filter} - пользователи с фильтром
/block_user {userId} - блокировка

VOICE MESSAGES:
- Для создания теста можно отправить голосовое
- n8n вызывает Groq Whisper для транскрибирования
- Затем Groq LLama для парсинга конфигурации
- Потом отправляет POST запрос в diplom-test-service

NOTIFICATIONS:
/send_email {testId} {variant} - отправить письмо группе
/send_telegram {message} - отправить в чат
```

### Workflow в n8n

```
Триггер: Telegram message
├─ Если текст команда
│  └─ Парсим команду → вызываем API
├─ Если голос
│  ├─ Transcribe (Whisper API)
│  ├─ Parse (LLama API)
│  └─ Create test (HTTP request)
└─ Если файл
   └─ Upload и сохранить URL

Каждый вызов API:
- REST API call к нужному микросервису
- Обработка ответа
- Отправка результата в Telegram
```

---

## 7. МЕТРИКИ И ГРАФИКИ

### Сбор метрик (diplom-shop)

```
UserEvent Entity:
{
  id: String (UUID),
  userId: String,
  eventType: "page_view" | "click" | "scroll" | "time_spent" | "order_completed",
  eventData: JSON,
  page: String (URL),
  testId: String (если участвует),
  variant: String,
  sessionId: String,
  userAgent: String,
  ipAddress: String,
  timestamp: DateTime
}

Сохраняется в diplom_shop.user_events (MongoDB)
```

### Аналитика (diplom-shop backend)

```
GET /admin/metrics/test/{testId}
- Основные метрики по тесту
- Ответ:
  {
    testId: "...",
    testName: "...",
    variants: {
      "A": {
        uniqueUsers: 750,
        pageViews: 3500,
        clicks: 1200,
        avgScrollDepth: 65,
        avgSessionMs: 180000,
        orders: 45,
        orderRevenue: 450000,
        conversionRate: 6.0
      },
      "B": {
        uniqueUsers: 750,
        pageViews: 3200,
        clicks: 1100,
        avgScrollDepth: 58,
        avgSessionMs: 160000,
        orders: 38,
        orderRevenue: 380000,
        conversionRate: 5.07
      }
    },
    abtestAnalysis: {
      winner: "A",
      confidence: 0.85,
      liftPercentage: 18.4
    }
  }

GET /admin/metrics/user/{userId}
- История активности пользователя

GET /admin/metrics/overview
- Общая статистика по магазину за период
```

### Графики (Grafana + Prometheus)

```
Метрики в формате Prometheus:
- ab_test_unique_users{test_id="...", variant="A"} = 750
- ab_test_page_views{test_id="...", variant="A"} = 3500
- ab_test_clicks{test_id="...", variant="A"} = 1200
- ab_test_orders{test_id="...", variant="A"} = 45
- ab_test_conversion_rate{test_id="...", variant="A"} = 6.0
- ab_test_revenue{test_id="...", variant="A"} = 450000
- ab_test_avg_scroll_pct{test_id="...", variant="A"} = 65.0
- ab_test_avg_session_ms{test_id="...", variant="A"} = 180000

Dasboards в Grafana:
1. A/B Test Overview - общая сводка по всем тестам
2. Test Details - детали конкретного теста (вариант A vs B)
3. User Behavior - поведение пользователей
4. Conversion Funnel - воронка конверсии
5. Revenue Analysis - анализ доходов
```

---

## ПОТОК ПОЛЬЗОВАТЕЛЯ

```
1. РЕГИСТРАЦИЯ
   User → /auth/register (login, password, email, имя, возраст, страна, язык, гендер)
   └─ Опционально: демографические данные, Telegram

2. ПРОСМОТР ПРОФИЛЯ
   GET /profile → показываем все данные
   PUT /profile → обновляем профиль
   PUT /profile/demographics → добавляем демографические данные позже

3. ПРОСМОТР ТОВАРОВ
   GET /products → список товаров
   GET /products/{id} → детали товара
   └─ Если пользователь в тесте A → показываем дизайн А
   └─ Если в тесте B → показываем дизайн B
   └─ Иначе → дефолтный дизайн

4. ДОБАВЛЕНИЕ В КОРЗИНУ
   POST /cart/add {productId, quantity}
   └─ Сохраняем в sessionStorage/базе

5. ОФОРМЛЕНИЕ ЗАКАЗА
   POST /checkout
   └─ Создается заказ в БД
   └─ Сохраняется testId и variant если участвует в тесте
   └─ POST /metrics/events {eventType: "order_completed"}
   └─ История заказов доступна в GET /orders

6. СБОР МЕТРИК ВСЕ ВРЕМЯ
   - На каждый page_view → POST /metrics/events
   - На каждый клик → POST /metrics/events
   - На скролл (throttled) → POST /metrics/events
   - На тайм-спент (каждые 5 сек) → POST /metrics/events
```

---

## ПОТОК АДМИНИСТРАТОРА

```
1. ЛОГИН
   /auth/login → JWT с ROLE_ADMIN

2. ДОБАВЛЕНИЕ ТОВАРОВ
   POST /admin/products {name, description, price, photo}

3. УПРАВЛЕНИЕ ТОВАРАМИ
   PUT /admin/products/{id} → изменение
   PUT /admin/products/{id}/stock → изменение количества
   DELETE /admin/products/{id} → скрытие

4. СОЗДАНИЕ ТЕСТА
   POST /admin/ab-tests {name, variants, rules, expiresAt}
   ├─ rules определяют: какого возраста, стран, пола участвуют
   └─ При сохранении тест в статусе DRAFT

5. ЗАГРУЗКА ШАБЛОНОВ
   POST /admin/templates {testId, variant, htmlContent, targetPage}

6. ЗАПУСК ТЕСТА
   POST /admin/ab-tests/{testId}/start
   ├─ POST diplom-test-service /api/tests/{testId}/trigger-selection
   ├─ diplom-selector-service выбирает пользователей из GlobalKTable
   ├─ Сохраняет в TestParticipation
   └─ Тест переходит в статус RUNNING

7. ПРОСМОТР МЕТРИК
   GET /admin/metrics/test/{testId}
   └─ Видим распределение по вариантам и основные KPI

8. ОСТАНОВКА/ПЕРЕЗАПУСК ТЕСТА
   POST /admin/ab-tests/{testId}/stop
   POST /admin/ab-tests/{testId}/restart

9. СОЗДАНИЕ РАССЫЛКИ
   POST /api/campaigns {name, channel, template, testId, variant}
   POST /api/campaigns/{id}/send → отправляем письма/сообщения группе

10. ТЕЛЕГРАМ АДМИНКА
    /create_test → интерактивное создание
    /test_stats {testId} → метрики
    Голосовое сообщение → парсим конфигурацию и создаем тест
```

---

## ПОТОК ТЕСТА

```
1. СОЗДАНИЕ КОНФИГУРАЦИИ
   Admin → POST /admin/ab-tests
   └─ Определяют: название, варианты (A 50%, B 50%), правила для фильтра
   └─ Статус: DRAFT
   └─ Загружают шаблоны для каждого варианта

2. ЗАПУСК СЕЛЕКЦИИ
   Admin → POST /admin/ab-tests/{testId}/start
   └─ Отправляем событие в Kafka topic "test-selection-requests"

3. СЕЛЕКТОР РАБОТАЕТ
   diplom-selector-service:
   ├─ Получил SelectionRequest из Kafka
   ├─ Зачитал ALL пользователей из GlobalKTable (user-profiles)
   ├─ Применил правила фильтрации (возраст, страна, язык, гендер)
   ├─ Для advanced criteria вызвал diplom-demographic-service API
   ├─ Разделил отобранных пользователей на группы (A: 50%, B: 50%)
   ├─ Создал TestParticipant записи в БД
   └─ Отправил TestParticipantEvent в Kafka

4. РЕЗУЛЬТАТЫ ОТПРАВЛЕНЫ
   diplom-test-service получил события
   └─ Обновил статистику теста
   └─ Вернул результат админу

5. ТЕСТ АКТИВЕН
   Статус: RUNNING/ACTIVE
   └─ Пользователи заходят на сайт
   └─ diplom-shop проверяет: есть ли в TestParticipation?
   └─ Если да → возвращаем вариант → показываем нужный шаблон
   └─ Если нет → дефолтный шаблон

6. СБОР МЕТРИК
   На каждое действие пользователя сохраняем событие:
   POST /metrics/events
   ├─ eventType: page_view | click | order_completed
   ├─ testId: (из TestParticipation)
   ├─ variant: "A" или "B"
   └─ Все метрики сохраняются в user_events

7. ПРОСМОТР МЕТРИК
   Admin → GET /admin/metrics/test/{testId}
   └─ Видим KPI по каждому варианту
   └─ Можем увидеть winner (статистически значимый)

8. ЗАВЕРШЕНИЕ ТЕСТА
   Admin → POST /admin/ab-tests/{testId}/complete
   └─ Тест переходит в статус COMPLETED
   └─ Новые пользователи больше не распределяются
   └─ Старые пользователи могут дальше видеть свой вариант
```

---

## ТРЕБУЕМЫЕ ИНТЕГРАЦИИ МЕЖДУ СЕРВИСАМИ

### HTTP Calls

```
diplom-shop → diplom-test-service:
  GET /api/tests/{testId}/resolve?userId={userId}&path={path}
  └─ Проверить, участвует ли пользователь в тесте

diplom-shop → diplom-demographic-service:
  GET /api/profiles/{userId}
  POST /api/demographics (сохранить демографические данные пользователя)

diplom-test-service → diplom-demographic-service:
  GET /api/profiles/{userId} (при проверке критериев)

diplom-selector-service → diplom-demographic-service:
  POST /api/demographics/bulk (получить данные много пользователей)
  └─ Для проверки advanced criteria (income, education, occupation, interests)
```

### Kafka Topics

```
user-registered (source: diplom-shop, consumers: diplom-selector-service, diplom-test-service)
  └─ Новый пользователь зарегистрировался

user-profiles (source: diplom-shop, consumers: diplom-selector-service)
  └─ Обновление демографических данных пользователя
  └─ Используется как GlobalKTable в diplom-selector-service

test-selection-requests (source: diplom-test-service, consumer: diplom-selector-service)
  └─ Запрос на селекцию пользователей для теста

test-participants-result (source: diplom-selector-service, consumer: diplom-test-service)
  └─ Результаты селекции (кто в какую группу попал)

user-events (source: diplom-shop, consumer: metrics storage)
  └─ Клики, просмотры, скроллы, заказы

notification-sent (source: diplom-notification-service, consumer: analytics)
  └─ Отправлены письма (для отслеживания результативности кампаний)
```

---

## ТРЕБУЕМЫЕ ТАБЛИЦЫ / COLLECTIONS

### diplom_shop (основная БД)

```
users
  id: ObjectId
  login: String (unique)
  password: String (BCrypt hash)
  email: String (optional)
  firstName: String
  lastName: String
  age: Integer
  country: String
  language: String
  gender: Gender enum
  phone: String (optional)
  telegramChatId: String (optional)
  status: ACTIVE | BLOCKED
  createdAt: DateTime
  updatedAt: DateTime

products
  id: ObjectId
  name: String
  description: String
  price: Decimal
  availableQuantity: Integer
  photoKey: String (для S3)
  photoUrl: String
  category: String (optional)
  createdAt: DateTime
  updatedAt: DateTime

orders
  id: ObjectId
  userId: ObjectId (ref users)
  items: [
    {
      productId: ObjectId,
      productName: String,
      price: Decimal,
      quantity: Integer
    }
  ]
  totalPrice: Decimal
  status: PENDING | COMPLETED | CANCELLED
  testId: String (optional, если пользователь в тесте)
  variant: String (optional, A/B/C/D)
  createdAt: DateTime

user_test_participations
  id: ObjectId
  testId: String
  userId: ObjectId
  variant: String
  enrolledAt: DateTime
  unique index: (testId, userId)

user_events (MetricsService)
  id: ObjectId
  userId: ObjectId
  eventType: String (page_view, click, scroll, time_spent, order_completed)
  eventData: JSON
  page: String (URL)
  testId: String (optional)
  variant: String (optional)
  sessionId: String
  userAgent: String
  ipAddress: String
  timestamp: DateTime
  indexes: (userId, timestamp), (testId, variant)

test_templates (HTMLTemplates for A/B tests)
  id: ObjectId
  testId: String
  variant: String
  name: String
  htmlContent: String (или minioKey)
  targetPage: String
  createdAt: DateTime
  index: (testId, variant)
```

### diplom_tests (test configuration БД)

```
ab_tests
  id: String
  name: String
  description: String (optional)
  variants: {
    "A": { probability: 50, rules: [] },
    "B": { probability: 50, rules: [] }
  }
  rules: [
    {
      id: String,
      priority: Integer,
      userId: String (optional),
      pathPattern: String,
      active: Boolean,
      conditions: {
        ageMin, ageMax,
        countries: [],
        languages: [],
        genders: []
      }
    }
  ]
  status: DRAFT | RUNNING | ACTIVE | COMPLETED
  createdAt: DateTime
  startedAt: DateTime (optional)
  expiresAt: DateTime (optional)
  endedAt: DateTime (optional)
```

### diplom_tests.test_participants (Kafka Streams output)

```
test_participants
  id: ObjectId
  testId: String
  userId: String
  variant: String
  enrolledAt: DateTime
  index: (testId, userId) unique, (testId)
```

### diplom_demographics (user demographics БД)

```
user_demographics
  id: ObjectId
  userId: String (unique)
  incomeLevel: String (LOW, MEDIUM, HIGH)
  educationLevel: String (BASIC, SECONDARY, HIGHER, ACADEMIC)
  occupation: String (STUDENT, EMPLOYED, SELF_EMPLOYED, UNEMPLOYED, RETIRED)
  interests: [String]
  createdAt: DateTime
  updatedAt: DateTime
```

### diplom_notifications (campaigns и delivery БД)

```
notification_campaigns
  id: ObjectId
  name: String
  channel: String (EMAIL, TELEGRAM, BOTH)
  subject: String (optional, для email)
  body: String
  targetType: String (ALL, SPECIFIC, TEST_VARIANT)
  testId: String (optional)
  testVariant: String (optional)
  targetUserIds: [String] (optional)
  status: DRAFT | SENT | CANCELLED
  createdAt: DateTime
  sentAt: DateTime (optional)
  sentCount: Integer
  failedCount: Integer

notification_deliveries
  id: ObjectId
  campaignId: ObjectId (ref campaigns)
  userId: String
  channel: String
  recipient: String (email или telegram chat id)
  status: PENDING | DELIVERED | FAILED
  errorMessage: String (optional)
  createdAt: DateTime
  sentAt: DateTime (optional)
  deliveredAt: DateTime (optional)
```

---

## ПЛАНЫ РЕАЛИЗАЦИИ ПО ПРИОРИТЕТАМ

### PHASE 1: Core Shop + Basic Test Framework (2-3 недели)
- [x] Монореpo структура + 5 микросервисов (ГОТОВО)
- [ ] Authentication/Registration в diplom-shop
- [ ] Product Management (CRUD)
- [ ] Shopping Cart + Checkout
- [ ] Basic A/B routing (diplom-test-service)
- [ ] User participation tracking
- [ ] Basic metrics collection

### PHASE 2: Advanced Metrics + Selector (2 недели)
- [ ] Kafka Streams topology в diplom-selector-service
- [ ] Advanced filtering (demographic criteria)
- [ ] diplom-demographic-service integration
- [ ] Prometheus metrics exporting
- [ ] Grafana dashboards

### PHASE 3: Notifications + Telegram Admin (1.5 недели)
- [ ] Email campaigns (diplom-notification-service)
- [ ] Telegram integration
- [ ] n8n workflows
- [ ] Voice message support (Groq Whisper + LLama)

### PHASE 4: Polish + Optimization (1 неделя)
- [ ] UI improvements
- [ ] Performance tuning
- [ ] Security hardening
- [ ] Documentation

---

## ТЕХНОЛОГИЧЕСКИЙ СТЕК

```
Backend:
- Java 21, Spring Boot 3.2
- MongoDB 7.0 (5 databases)
- Apache Kafka (KRaft mode)
- Spring Data MongoDB
- Lombok, MapStruct
- JUnit 5 + Testcontainers

Message Processing:
- Kafka Streams (diplom-selector-service)
- KStream + GlobalKTable patterns

Notifications:
- Jakarta Mail (SMTP)
- Telegram Bot API
- n8n for workflows
- Groq API (Whisper + LLama)

Monitoring:
- Micrometer + Prometheus
- Grafana
- Spring Boot Actuator

Storage:
- MongoDB for persistence
- MinIO / AWS S3 for files

Frontend:
- Thymeleaf templates
- Vanilla JS (metrics collection)
- CSS for A/B variants
```

---

## NEXT STEPS

1. **Compile all services** → `mvn clean install` ✅
2. **Docker Compose** → Get all services running together
3. **Start with Phase 1** → Implement core shop functionality
4. **Test each endpoint** → Ensure basic flows work
5. **Integrate Kafka** → Connect services via events
6. **Add metrics** → Implement data collection
7. **Build admin UI** → Create admin dashboard

