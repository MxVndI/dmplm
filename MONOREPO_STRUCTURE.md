# 🏗️ DiplomShop - Монорепо Структура

## Обзор

Проект преобразован в **монорепо (monorepo)** структуру с общим родительским POM.

```
BIGGEST/
├── pom.xml                           ← РОДИТЕЛЬСКИЙ POM (diplom-parent)
├── diplom/                           ← Модуль: Main Service
│   ├── pom.xml                       (наследует от parent)
│   └── src/main/java/...
├── diplom-test-service/              ← Модуль: Test Service
│   ├── pom.xml                       (наследует от parent)
│   └── src/main/java/...
├── diplom-demographic-service/       ← Модуль: Demographics Service
│   ├── pom.xml                       (наследует от parent)
│   └── src/main/java/...
├── diplom-notification-service/      ← Модуль: Notification Service
│   ├── pom.xml                       (наследует от parent)
│   └── src/main/java/...
├── diplom-selector-service/          ← Модуль: Selector Service
│   ├── pom.xml                       (наследует от parent)
│   └── src/main/java/...
├── docker-compose.yml
├── REFACTORING_SUMMARY.md
├── STRUCTURE_CHECKLIST.md
├── CLEANUP_INSTRUCTIONS.md
└── ...
```

---

## Структура Родительского POM

### Версии зависимостей

```xml
<properties>
    <spring-boot.version>3.2.3</spring-boot.version>
    <java.version>21</java.version>
    <aws.sdk.version>2.24.6</aws.sdk.version>
    <mapstruct.version>1.5.5.Final</mapstruct.version>
    <lombok.version>1.18.30</lombok.version>
</properties>
```

### Управляемые зависимости (dependencyManagement)

Все версии определены в родительском POM:
- ✓ Spring Boot (web, data-mongodb, kafka, etc.)
- ✓ AWS SDK
- ✓ MapStruct
- ✓ Lombok
- ✓ Jackson

### Общие зависимости (dependencies)

Все модули автоматически получают:
- `lombok`
- `spring-boot-starter-test`
- `jackson-datatype-jsr310`

### Общие плагины (pluginManagement)

- ✓ spring-boot-maven-plugin
- ✓ maven-compiler-plugin (с MapStruct + Lombok processors)
- ✓ maven-surefire-plugin (для тестов)
- ✓ maven-shade-plugin (опционально)

---

## Структура Child POM (Микросервис)

**Минимальная конфигурация:**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project>
    <modelVersion>4.0.0</modelVersion>

    <!-- Указывает родительский POM -->
    <parent>
        <groupId>com.diplom</groupId>
        <artifactId>diplom-parent</artifactId>
        <version>1.0.0</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <groupId>com.diplom</groupId>
    <artifactId>diplom-shop</artifactId>  <!-- или другой сервис -->
    <version>1.0.0</version>
    <name>DiplomShop</name>
    <description>...</description>

    <!-- Зависимости ТОЛЬКО специфичные для этого сервиса -->
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <!-- ... другие специфичные зависимости -->
    </dependencies>
</project>
```

---

## Команды Maven

### Сборка всего проекта (все модули)

```bash
cd C:\Users\LesunVo\Desktop\BIGGEST

# Компиляция, тесты, упаковка
mvn clean package

# Только компиляция
mvn clean compile

# Только тесты
mvn clean test

# Без тестов
mvn clean package -DskipTests
```

### Сборка одного модуля

```bash
# Сборка только diplom
mvn clean package -pl diplom

# Сборка diplom + его зависимости
mvn clean package -pl diplom -am

# Сборка diplom + модули зависящие от него
mvn clean package -pl diplom -amd
```

### Профили сборки

```bash
# Development (по умолчанию)
mvn clean package -P dev

# Production
mvn clean package -P prod

# Docker
mvn clean package -P docker
```

### Проверка структуры

```bash
# Показать все модули
mvn --non-recursive exec:exec -Dexec.executable="echo" -Dexec.args="{project.modules}"

# Проверить зависимости
mvn dependency:tree

# Проверить для конкретного модуля
mvn dependency:tree -pl diplom
```

---

## Преимущества Монорепо

### 1️⃣ **Единая версионизация**
- Все модули используют одну версию: `1.0.0`
- Одна версия Spring Boot: `3.2.3`
- Легко обновлять все сразу

### 2️⃣ **Единое управление зависимостями**
- MapStruct, Lombok, AWS SDK версии в одном месте
- Нет конфликтов между версиями в разных сервисах
- Легче отслеживать обновления

### 3️⃣ **Единая конфигурация сборки**
- Один maven-compiler-plugin с MapStruct + Lombok
- Один spring-boot-maven-plugin для всех
- Консистентная сборка везде

### 4️⃣ **Упрощённая разработка**
- Клонируем один репо вместо 5
- `mvn clean package` собирает ВСЁ сразу
- IDE легче работать с зависимостями между модулями

### 5️⃣ **Лучший контроль версий**
- Атомарные коммиты для всего проекта
- Изменения в архитектуре затрагивают все модули сразу
- Git история чище

### 6️⃣ **CI/CD удобство**
- Один pipeline для всего проекта
- Параллельная сборка модулей
- Кэширование работает лучше

---

## Примеры использования

### Добавить зависимость во ВСЕ модули

**В родительский pom.xml → `<dependencies>`:**

```xml
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-lang3</artifactId>
    <version>3.14.0</version>
</dependency>
```

Все модули автоматически получат эту зависимость.

### Добавить зависимость только в один модуль

**В diplom/pom.xml:**

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
        <!-- Версия берётся из parent -->
    </dependency>
</dependencies>
```

### Добавить новый модуль

1. Создай папку: `diplom-new-service/`
2. Создай `pom.xml`:

```xml
<parent>
    <groupId>com.diplom</groupId>
    <artifactId>diplom-parent</artifactId>
    <version>1.0.0</version>
    <relativePath>../pom.xml</relativePath>
</parent>

<artifactId>diplom-new-service</artifactId>
<name>New Service</name>
```

3. Добавь в корневой pom.xml:

```xml
<modules>
    <!-- ... существующие модули ... -->
    <module>diplom-new-service</module>
</modules>
```

4. Запусти: `mvn clean package` — новый модуль соберётся с остальными!

---

## Файловая структура каждого модуля

```
diplom/
├── pom.xml                          (наследует от parent)
├── src/
│   ├── main/
│   │   ├── java/com/diplom/
│   │   │   ├── rest/                (REST Layer)
│   │   │   │   ├── controller/
│   │   │   │   └── dto/
│   │   │   ├── domain/              (Domain Layer)
│   │   │   │   ├── model/
│   │   │   │   └── service/
│   │   │   ├── persistance/         (Persistence Layer)
│   │   │   │   ├── entity/
│   │   │   │   └── repository/
│   │   │   ├── mapper/              (MapStruct)
│   │   │   ├── config/              (Spring config)
│   │   │   ├── utils/               (Utilities)
│   │   │   ├── event/               (Kafka events)
│   │   │   └── DiplomApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── ...
│   └── test/
│       └── java/com/diplom/...
├── target/                          (сборка)
└── Dockerfile                       (опционально)
```

---

## Обновление версий

### Обновить Spring Boot для всех модулей

**В корневом pom.xml:**

```xml
<spring-boot.version>3.3.0</spring-boot.version>
```

Затем: `mvn clean package` — все модули используют новую версию.

### Обновить MapStruct

**В корневом pom.xml:**

```xml
<mapstruct.version>1.6.0.Final</mapstruct.version>
```

---

## Текущая версионизация

| Компонент | Версия |
|-----------|--------|
| Project Version | 1.0.0 |
| Spring Boot | 3.2.3 |
| Java | 21 |
| MapStruct | 1.5.5.Final |
| Lombok | 1.18.30 |
| AWS SDK | 2.24.6 |

---

## Модули проекта

```
diplom-parent (1.0.0)
├── diplom (1.0.0)                    - Main E-commerce Service
├── diplom-test-service (1.0.0)      - A/B Test Management
├── diplom-demographic-service (1.0.0) - User Profiles
├── diplom-notification-service (1.0.0) - Email & Telegram
└── diplom-selector-service (1.0.0)  - Kafka Streams Processor
```

---

## Troubleshooting

### Ошибка: "Module not found"

```bash
# Убедись что находишься в root директории
cd C:\Users\LesunVo\Desktop\BIGGEST

# Проверь структуру
ls -la  # или dir на Windows
```

### Конфликт версий зависимостей

1. Проверь версию в корневом pom.xml
2. Удали `<version>` из child pom.xml (возьмёт из parent)
3. Очисти: `mvn clean`

### MapStruct не генерирует классы

1. Убедись что в parent pom.xml есть annotationProcessorPaths
2. Пересоберись: `mvn clean compile`
3. Проверь `target/generated-sources/annotations/`

---

## Лучшие практики

✓ **Делай:** Определяй версии в parent pom.xml  
✗ **Не делай:** Копируй версии в child pom.xml  

✓ **Делай:** Используй `<relativePath>../pom.xml</relativePath>`  
✗ **Не делай:** Полный path в parent  

✓ **Делай:** Общие зависимости в `<dependencies>`  
✗ **Не делай:** Дублируй в каждом child  

✓ **Делай:** Специфичные зависимости только в нужном модуле  
✗ **Не делай:** Добавляй всё в parent  

---

## Готово! 🚀

Теперь у тебя есть:
- ✓ Единая версионизация
- ✓ Единая конфигурация сборки
- ✓ Простое управление зависимостями
- ✓ Масштабируемость для добавления новых модулей
- ✓ Консистентная структура всех сервисов

**Команда для полной сборки:**
```bash
mvn clean package -DskipTests
```

Good luck! 💪
