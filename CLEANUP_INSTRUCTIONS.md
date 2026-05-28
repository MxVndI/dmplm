# 🗑️ Инструкция по удалению старых папок

## Обзор

После рефакторинга всех 5 микросервисов на трёхслойную архитектуру остались старые папки, которые больше не используются.

**Каждый сервис имеет старые папки:**
- `model/` → Перемещено в `domain/model/` и `persistance/entity/`
- `controller/` → Перемещено в `rest/controller/`
- `service/` → Перемещено в `domain/service/` и `utils/`
- `repository/` → Перемещено в `persistance/repository/`
- `dto/` → Перемещено в `rest/dto/`
- `security/` → Перемещено в `config/`
- `consumer/` → Перемещено или удалено (для test-service)
- `processor/` → Перемещено в `stream/` (для selector-service)

---

## Способ 1: Автоматический (Рекомендуется)

### Windows (PowerShell)

1. **Откройте PowerShell с правами администратора**
   - Win + X → Windows PowerShell (Admin)

2. **Перейдите в папку проекта:**
   ```powershell
   cd C:\Users\LesunVo\Desktop\BIGGEST
   ```

3. **Запустите скрипт очистки:**
   ```powershell
   Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process -Force
   .\cleanup_old_dirs.ps1
   ```

4. **Проверьте результат:**
   - Скрипт выведет список удалённых папок для каждого сервиса

### Windows (Batch/CMD)

1. **Откройте CMD с правами администратора**
   - Win + X → Command Prompt (Admin)

2. **Перейдите в папку проекта:**
   ```cmd
   cd C:\Users\LesunVo\Desktop\BIGGEST
   ```

3. **Запустите батник:**
   ```cmd
   cleanup_old_dirs.bat
   ```

### Linux/Mac (Bash)

1. **Откройте терминал**

2. **Перейдите в папку проекта:**
   ```bash
   cd /path/to/BIGGEST  # или /sessions/nice-festive-cray/mnt/BIGGEST
   ```

3. **Дайте права на выполнение и запустите:**
   ```bash
   chmod +x cleanup_old_dirs.sh
   ./cleanup_old_dirs.sh
   ```

---

## Способ 2: Ручное удаление (Если скрипт не сработал)

### Для каждого сервиса:

#### diplom
```
Удали эти папки в: src/main/java/com/diplom/
- model/
- controller/
- service/
- repository/
- dto/
- security/
```

#### diplom-test-service
```
Удали эти папки в: src/main/java/com/diplom/testservice/
- model/
- controller/
- service/
- repository/
- dto/
- consumer/
```

#### diplom-demographic-service
```
Удали эти папки в: src/main/java/com/diplom/demographic/
- model/
- controller/
- service/
- repository/
- dto/
```

#### diplom-notification-service
```
Удали эти папки в: src/main/java/com/diplom/notification/
- model/
- controller/
- service/
- repository/
- dto/
```

#### diplom-selector-service
```
Удали эти папки в: src/main/java/com/diplom/selector/
- model/
- controller/
- repository/
- processor/
```

---

## Проверка после удаления

### 1. Убедись что все старые папки удалены:
```bash
# Linux/Mac
find . -path "*/java/com/diplom/*" -type d -name "model" -o -name "controller"

# Windows (PowerShell)
Get-ChildItem -Path "C:\Users\LesunVo\Desktop\BIGGEST" -Recurse -Directory -Filter "model" | Where-Object { $_.FullPath -like "*java*" }
```

### 2. Проверь что новая структура на месте:
```bash
# Linux/Mac
find . -path "*/java/com/diplom/rest" -type d
find . -path "*/java/com/diplom/domain" -type d
find . -path "*/java/com/diplom/persistance" -type d

# Windows (PowerShell)
Get-ChildItem -Path "C:\Users\LesunVo\Desktop\BIGGEST" -Recurse -Directory -Filter "rest" | Where-Object { $_.FullPath -like "*java*" }
```

### 3. Попробуй скомпилировать проект:
```bash
cd C:\Users\LesunVo\Desktop\BIGGEST\diplom
mvn clean package -DskipTests
```

---

## FAQ

**Q: Что если скрипт скажет "Access Denied"?**  
A: Убедись что:
1. Запустил с правами администратора
2. Все файлы не открыты в IDE (закрой IntelliJ/VS Code)
3. Git не заблокировал папки (сделай `git status`)

**Q: Можно ли восстановить старые папки?**  
A: Да! Все файлы уже скопированы в новую структуру. Старые папки - это просто дубликаты.
Если нужно, можно восстановить из git: `git checkout -- .`

**Q: Что если я случайно удалил не то?**  
A: Git поможет! `git status` покажет удалённые файлы, `git restore <path>` восстановит.

**Q: Нужно ли менять что-то в pom.xml?**  
A: Нет! Уже всё обновлено (добавлен MapStruct, обновлены paths компилятора).

---

## После удаления

✓ Проект чистый и организованный  
✓ Новая трёхслойная архитектура на месте  
✓ Готов к компиляции: `mvn clean package`  
✓ Готов к развёртыванию  

---

## Дальше

После успешной очистки:

1. **Скомпилируй:** `mvn clean package`
2. **Протестируй:** `docker-compose up`
3. **Изучи требования:** Прочти НИР (научно-исследовательскую работу)
4. **Валидируй:** Убедись что архитектура соответствует требованиям

Good luck! 🚀
