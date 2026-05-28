# ⚡ Quick Start - DiplomShop Monorepo

## Что готово?

✅ **Трёхслойная архитектура** для всех 5 микросервисов  
✅ **Монорепо** с единым родительским POM  
✅ **MapStruct** для маппинга между слоями  
✅ **Единая версионизация** (Spring Boot 3.2.3, Java 21)  

---

## 🚀 За 5 минут до первого запуска

### Шаг 1: Очистить старые папки

**Windows (PowerShell с правами администратора):**
```powershell
cd C:\Users\LesunVo\Desktop\BIGGEST
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process -Force
.\cleanup_old_dirs.ps1
```

**Linux/Mac:**
```bash
cd /path/to/BIGGEST
chmod +x cleanup_old_dirs.sh
./cleanup_old_dirs.sh
```

### Шаг 2: Собрать весь проект

```bash
cd C:\Users\LesunVo\Desktop\BIGGEST
mvn clean package -DskipTests
```

**Что произойдёт:**
- ✓ Скачаются все зависимости
- ✓ Скомпилируются все 5 микросервисов
- ✓ MapStruct сгенерирует маперы
- ✓ Создадутся JAR файлы в каждом target/

### Шаг 3: Запустить в Docker

```bash
docker-compose up -d
```

**Сервисы будут доступны:**
- Shop: http://localhost:8080
- Test Service: http://localhost:8081
- Selector Service: http://localhost:8082
- Demographic Service: http://localhost:8084
- Notification Service: http://localhost:8083
- n8n: http://localhost:5678
- Grafana: http://localhost:3000 (admin/admin)
- MinIO: http://localhost:9001 (minioadmin/minioadmin)

---

## 📁 Структура проекта

```
BIGGEST/
├── pom.xml                          ← Родительский POM (diplom-parent)
│
├── diplom/                          ← Main Service
├── diplom-test-service/             ← A/B Tests
├── diplom-demographic-service/      ← User Profiles
├── diplom-notification-service/     ← Email/Telegram
├── diplom-selector-service/         ← Kafka Streams
│
├── docker-compose.yml               ← Orchestration
├── MONOREPO_STRUCTURE.md            ← Полная документация
├── CLEANUP_INSTRUCTIONS.md          ← Как удалить старые папки
├── REFACTORING_SUMMARY.md           ← Что было сделано
└── QUICK_START.md                   ← Этот файл
```

---

## 🔧 Часто используемые команды

### Сборка

```bash
# Всё
mvn clean package -DskipTests

# Один модуль
mvn clean package -pl diplom -DskipTests

# Только компиляция
mvn clean compile

# С тестами
mvn clean package
```

### Запуск IDE

```bash
# IntelliJ IDEA
# 1. File → Open → выбери C:\Users\LesunVo\Desktop\BIGGEST
# 2. IDE автоматически узнает монорепо
# 3. Можешь запускать приложения прямо из IDE

# VS Code
# 1. Открой папку BIGGEST
# 2. Установи Extension Pack for Java
# 3. VS Code поддерживает монорепо нативно
```

### Запуск одного сервиса локально

```bash
# Terminal 1: Kafka + MongoDB + MinIO + Grafana
docker-compose up -d

# Terminal 2: diplom (main service)
cd diplom
mvn spring-boot:run

# Доступен на http://localhost:8080
```

### Логирование

```bash
# Логи контейнера
docker-compose logs -f diplom

# Логи приложения (если запущено локально)
# Смотри в IDE консоль или terminal
```

---

## 📐 Архитектура каждого сервиса

Каждый из 5 сервисов имеет эту структуру:

```
src/main/java/com/diplom[/SERVICE]/
├── rest/                ← HTTP слой
│   ├── controller/      (REST контроллеры)
│   └── dto/             (Request/Response DTOs)
├── domain/              ← Бизнес логика
│   ├── model/           (Чистые POJO модели)
│   └── service/         (Бизнес сервисы)
├── persistance/         ← Работа с БД
│   ├── entity/          (MongoDB документы)
│   └── repository/      (Spring Data репо)
├── mapper/              ← MapStruct конвертеры
├── config/              ← Spring конфиг
├── utils/               ← Помощники
└── event/               ← Kafka события
```

---

## 🔄 Workflow разработки

### 1. Модифицировать код

```bash
# Отредактируй файл в IDE
# Например: diplom/src/main/java/com/diplom/rest/controller/ProductController.java
```

### 2. Пересобрать

```bash
# Быстрая пересборка (в IDE обычно F5/Ctrl+B)
mvn clean compile

# Или если используешь hot-reload:
# Spring Boot DevTools автоматически пересоберёт
```

### 3. Перезапустить сервис

```bash
# Если запускал через docker-compose
docker-compose restart diplom

# Если запускал через IDE
# Просто перезапусти конфигурацию в IDE
```

---

## 📊 MapStruct Маперы

Уже созданы маперы для:
- `UserMapper` - User entity ↔ DTO
- `ProductMapper` - Product entity ↔ DTO
- `OrderMapper` - Order entity
- `ABTestMapper` - ABTest entity
- `UserTestParticipationMapper` - Participation entity
- `TestTemplateMapper` - Template entity
- `UserEventMapper` - Event entity

**Использование в коде:**

```java
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;
    
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody UserRegistrationDto dto) {
        User domain = userMapper.restToDomain(dto);
        User saved = userService.register(domain);
        return ResponseEntity.ok(saved);
    }
}
```

---

## 🗂️ Структура данных

### MongoDB Collections

```
diplom_shop:
├── users
├── products
├── orders
├── ab_tests
├── user_test_participants
├── test_templates
└── user_events

diplom_tests:
├── ab_tests (конфигурация)
├── ab_rules
├── ab_assignments
└── test_participants

diplom_demographics:
└── user_demographics

diplom_notifications:
├── notification_campaigns
└── notification_deliveries

diplom_selector: (в памяти, Kafka Streams)
└── (использует глобальные таблицы из других БД)
```

---

## ⚙️ Системные требования

- **Java:** 21+
- **Maven:** 3.9+
- **Docker:** 20.10+
- **Docker Compose:** 2.0+
- **RAM:** 4GB минимум
- **Disk:** 5GB для всех контейнеров

---

## 🐛 Troubleshooting

### Ошибка: "Could not find artifact"
```bash
mvn clean install -DskipTests
```

### Ошибка: "Port already in use"
```bash
# Найди какой процесс использует порт
lsof -i :8080  # macOS/Linux
netstat -ano | findstr :8080  # Windows

# Или используй другой порт в docker-compose.yml
```

### MapStruct не генерирует маперы
```bash
mvn clean compile
# Проверь: target/generated-sources/annotations/
```

### IDE не видит новые файлы
```bash
# IntelliJ IDEA: File → Invalidate Caches
# VS Code: Reload Window (Ctrl+Shift+P → Reload)
```

---

## 📚 Документация

| Файл | Описание |
|------|---------|
| `MONOREPO_STRUCTURE.md` | Подробная архитектура монорепо |
| `STRUCTURE_CHECKLIST.md` | Чек-лист всех 5 микросервисов |
| `REFACTORING_SUMMARY.md` | Что было рефакторено и почему |
| `CLEANUP_INSTRUCTIONS.md` | Как удалить старые папки |
| `CLAUDE.md` | Требования проекта (в корне BIGGEST) |

---

## 📞 Полезные ссылки

- **Spring Boot:** https://spring.io/projects/spring-boot
- **MapStruct:** https://mapstruct.org/
- **MongoDB:** https://docs.mongodb.com/
- **Kafka:** https://kafka.apache.org/
- **Docker Compose:** https://docs.docker.com/compose/

---

## ✅ Чек-лист перед запуском

- [ ] Java 21 установлена: `java -version`
- [ ] Maven установлен: `mvn -version`
- [ ] Docker установлен: `docker --version`
- [ ] Docker Compose установлен: `docker-compose --version`
- [ ] Нет старых папок (запустил cleanup скрипт)
- [ ] Проект собрался: `mvn clean package -DskipTests`
- [ ] docker-compose.yml находится в BIGGEST/
- [ ] Может запустить: `docker-compose up`

---

## 🎉 Готово!

Теперь ты можешь:

1. **Разрабатывать** - Все 5 сервисов под одной крышей
2. **Тестировать** - docker-compose запускает всю стек
3. **Развёртывать** - Monorepo упрощает CI/CD
4. **Масштабировать** - Легко добавлять новые модули

**Начни с:**
```bash
cd C:\Users\LesunVo\Desktop\BIGGEST
mvn clean package -DskipTests
docker-compose up
```

## 📖 Дальше

Следующие шаги:
1. ✅ Прочитай требования в НИР (задача #1)
2. ✅ Убедись что архитектура соответствует спецификации
3. ✅ Напиши тесты для сервисов
4. ✅ Настрой CI/CD pipeline

Good luck! 🚀
