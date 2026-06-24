package com.diplom.config;

import com.diplom.persistance.entity.ABTestEntity;
import com.diplom.persistance.entity.Gender;
import com.diplom.persistance.entity.ProductEntity;
import com.diplom.persistance.entity.UserEntity;
import com.diplom.persistance.repository.ABTestRepository;
import com.diplom.persistance.repository.ProductRepository;
import com.diplom.persistance.repository.UserRepository;
import com.diplom.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ABTestRepository abTestRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    @Override
    public void run(String... args) {
        seedAdmin();
        seedUsers();
        seedProducts();
        seedABTests();
        republishAllUserProfiles();
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
    }

    private void seedAdmin() {
        if (userRepository.existsByLogin("admin")) return;
        UserEntity admin = buildUser("admin", "Admin1234!", "Админ", "Демо",
                "Россия", "русский", Gender.OTHER, 30, "+7 999 000-00-00",
                "admin@diplom.local", Set.of("ROLE_USER", "ROLE_ADMIN"));
        UserEntity saved = userRepository.save(admin);
        userService.publishProfile(saved);
        log.warn("Создан демонстрационный администратор: login=admin, password=Admin1234!. Пароль необходимо изменить.");
    }

    private void seedUsers() {
        if (userRepository.existsByLogin("ivan_petrov")) {
            log.info("Демонстрационные пользователи уже созданы, пропуск.");
            return;
        }

        List<UserEntity> users = List.of(
                buildUser("ivan_petrov", "Pass1234!", "Иван", "Петров", "Россия", "русский", Gender.MALE, 25, "+7 911 111-11-11", "ivan.petrov@mail.ru", Set.of("ROLE_USER")),
                buildUser("maria_ivanova", "Pass1234!", "Мария", "Иванова", "Россия", "русский", Gender.FEMALE, 32, "+7 922 222-22-22", "maria.ivanova@mail.ru", Set.of("ROLE_USER")),
                buildUser("alex_sidorov", "Pass1234!", "Алексей", "Сидоров", "Россия", "русский", Gender.MALE, 19, "+7 933 333-33-33", "alex.sidorov@yandex.ru", Set.of("ROLE_USER")),
                buildUser("olga_koval", "Pass1234!", "Ольга", "Коваль", "Россия", "русский", Gender.FEMALE, 27, "+7 944 111-22-33", "olga.koval@gmail.com", Set.of("ROLE_USER")),
                buildUser("dmitry_novo", "Pass1234!", "Дмитрий", "Новосёлов", "Россия", "русский", Gender.MALE, 41, "+7 944 444-44-44", "dmitry.novo@mail.ru", Set.of("ROLE_USER")),
                buildUser("anna_white", "Pass1234!", "Анна", "Белова", "Россия", "русский", Gender.FEMALE, 23, "+7 900 000-00-06", "anna.white@mail.ru", Set.of("ROLE_USER")),
                buildUser("john_smith", "Pass1234!", "Иван", "Смирнов", "Россия", "русский", Gender.MALE, 35, "+7 900 000-00-07", "john.smith@mail.ru", Set.of("ROLE_USER")),
                buildUser("sophie_martin", "Pass1234!", "Софья", "Мартынова", "Россия", "русский", Gender.FEMALE, 29, "+7 900 000-00-08", "sophie.martin@mail.ru", Set.of("ROLE_USER")),
                buildUser("ali_hassan", "Pass1234!", "Али", "Хасанов", "Россия", "русский", Gender.MALE, 22, "+7 900 000-00-09", "ali.hassan@mail.ru", Set.of("ROLE_USER")),
                buildUser("zara_bekova", "Pass1234!", "Зара", "Бекова", "Россия", "русский", Gender.FEMALE, 18, "+7 900 000-00-10", "zara.bekova@mail.ru", Set.of("ROLE_USER")),
                buildUser("sergey_mak", "Pass1234!", "Сергей", "Макаров", "Россия", "русский", Gender.MALE, 37, "+7 900 000-00-11", "sergey.mak@mail.ru", Set.of("ROLE_USER")),
                buildUser("elena_volkov", "Pass1234!", "Елена", "Волкова", "Россия", "русский", Gender.FEMALE, 45, "+7 900 000-00-12", "elena.volkov@mail.ru", Set.of("ROLE_USER")),
                buildUser("nikita_frost", "Pass1234!", "Никита", "Морозов", "Россия", "русский", Gender.MALE, 20, "+7 900 000-00-13", "nikita.frost@mail.ru", Set.of("ROLE_USER")),
                buildUser("yuki_tanaka", "Pass1234!", "Юлия", "Тихонова", "Россия", "русский", Gender.FEMALE, 26, "+7 900 000-00-14", "yuki.tanaka@mail.ru", Set.of("ROLE_USER")),
                buildUser("carlos_garcia", "Pass1234!", "Кирилл", "Грачёв", "Россия", "русский", Gender.MALE, 33, "+7 900 000-00-15", "carlos.garcia@mail.ru", Set.of("ROLE_USER"))
        );

        for (UserEntity u : users) {
            if (!userRepository.existsByLogin(u.getLogin())) {
                UserEntity saved = userRepository.save(u);
                userService.publishProfile(saved);
            }
        }
        log.info("Создано демонстрационных пользователей: {}.", users.size());
    }

    private static final int EXPECTED_PRODUCT_COUNT = 32;

    private void seedProducts() {
        long count = productRepository.count();
        if (count >= EXPECTED_PRODUCT_COUNT) {
            log.info("Каталог уже заполнен: {} товар(ов), пропуск.", count);
            return;
        }
        if (count > 0) {
            productRepository.deleteAll();
            log.info("Удалено старых товаров для повторного заполнения каталога: {}.", count);
        }

        List<ProductEntity> products = List.of(
                buildProduct("Беспроводные наушники с шумоподавлением",
                        new BigDecimal("14990.00"), 50,
                        "Демонстрационный товар для каталога DiplomShop с описанием на русском языке."),
                buildProduct("Механическая клавиатура с RGB-подсветкой",
                        new BigDecimal("8990.00"), 120,
                        "Демонстрационный товар для каталога DiplomShop с описанием на русском языке."),
                buildProduct("Беспроводные TWS-наушники с ANC",
                        new BigDecimal("7990.00"), 150,
                        "Демонстрационный товар для каталога DiplomShop с описанием на русском языке."),
                buildProduct("Игровая гарнитура 7.1",
                        new BigDecimal("10900.00"), 45,
                        "Демонстрационный товар для каталога DiplomShop с описанием на русском языке."),
                buildProduct("USB-микрофон для стриминга",
                        new BigDecimal("12900.00"), 25,
                        "Демонстрационный товар для каталога DiplomShop с описанием на русском языке."),
                buildProduct("Игровая мышь с подсветкой",
                        new BigDecimal("5490.00"), 80,
                        "Демонстрационный товар для каталога DiplomShop с описанием на русском языке."),
                buildProduct("Коврик для мыши XL",
                        new BigDecimal("1990.00"), 160,
                        "Демонстрационный товар для каталога DiplomShop с описанием на русском языке."),
                buildProduct("Веб-камера Full HD",
                        new BigDecimal("4990.00"), 70,
                        "Демонстрационный товар для каталога DiplomShop с описанием на русском языке."),
                buildProduct("Портативная колонка",
                        new BigDecimal("6990.00"), 90,
                        "Демонстрационный товар для каталога DiplomShop с описанием на русском языке."),
                buildProduct("Смарт-часы",
                        new BigDecimal("12990.00"), 60,
                        "Демонстрационный товар для каталога DiplomShop с описанием на русском языке."),
                buildProduct("Фитнес-браслет",
                        new BigDecimal("3490.00"), 100,
                        "Демонстрационный товар для каталога DiplomShop с описанием на русском языке."),
                buildProduct("Повербанк 20000 мА·ч",
                        new BigDecimal("4490.00"), 120,
                        "Демонстрационный товар для каталога DiplomShop с описанием на русском языке."),
                buildProduct("Быстрое сетевое зарядное устройство",
                        new BigDecimal("2490.00"), 170,
                        "Демонстрационный товар для каталога DiplomShop с описанием на русском языке."),
                buildProduct("Кабель USB-C 100 Вт",
                        new BigDecimal("990.00"), 220,
                        "Демонстрационный товар для каталога DiplomShop с описанием на русском языке."),
                buildProduct("Док-станция USB-C",
                        new BigDecimal("6990.00"), 70,
                        "Демонстрационный товар для каталога DiplomShop с описанием на русском языке."),
                buildProduct("Внешний SSD 1 ТБ",
                        new BigDecimal("8990.00"), 80,
                        "Демонстрационный товар для каталога DiplomShop с описанием на русском языке."),
                buildProduct("Карта памяти 256 ГБ",
                        new BigDecimal("2490.00"), 140,
                        "Демонстрационный товар для каталога DiplomShop с описанием на русском языке."),
                buildProduct("Wi-Fi роутер",
                        new BigDecimal("5990.00"), 60,
                        "Демонстрационный товар для каталога DiplomShop с описанием на русском языке."),
                buildProduct("Умная лампа",
                        new BigDecimal("1290.00"), 130,
                        "Демонстрационный товар для каталога DiplomShop с описанием на русском языке."),
                buildProduct("Умная розетка",
                        new BigDecimal("990.00"), 160,
                        "Демонстрационный товар для каталога DiplomShop с описанием на русском языке."),
                buildProduct("Камера видеонаблюдения",
                        new BigDecimal("3990.00"), 50,
                        "Демонстрационный товар для каталога DiplomShop с описанием на русском языке."),
                buildProduct("Робот-пылесос",
                        new BigDecimal("34990.00"), 18,
                        "Демонстрационный товар для каталога DiplomShop с описанием на русском языке."),
                buildProduct("Электронная книга",
                        new BigDecimal("11990.00"), 45,
                        "Демонстрационный товар для каталога DiplomShop с описанием на русском языке."),
                buildProduct("Планшет 10 дюймов",
                        new BigDecimal("24990.00"), 35,
                        "Демонстрационный товар для каталога DiplomShop с описанием на русском языке."),
                buildProduct("Ноутбук для работы",
                        new BigDecimal("54990.00"), 20,
                        "Демонстрационный товар для каталога DiplomShop с описанием на русском языке."),
                buildProduct("Игровой монитор 27 дюймов",
                        new BigDecimal("29990.00"), 16,
                        "Демонстрационный товар для каталога DiplomShop с описанием на русском языке."),
                buildProduct("Монитор 4K",
                        new BigDecimal("39990.00"), 14,
                        "Демонстрационный товар для каталога DiplomShop с описанием на русском языке."),
                buildProduct("Саундбар",
                        new BigDecimal("17990.00"), 30,
                        "Демонстрационный товар для каталога DiplomShop с описанием на русском языке."),
                buildProduct("Геймпад",
                        new BigDecimal("4990.00"), 75,
                        "Демонстрационный товар для каталога DiplomShop с описанием на русском языке."),
                buildProduct("VR-очки",
                        new BigDecimal("29990.00"), 12,
                        "Демонстрационный товар для каталога DiplomShop с описанием на русском языке."),
                buildProduct("Фотоаппарат компактный",
                        new BigDecimal("45990.00"), 10,
                        "Демонстрационный товар для каталога DiplomShop с описанием на русском языке."),
                buildProduct("Стабилизатор для смартфона",
                        new BigDecimal("7990.00"), 55,
                        "Демонстрационный товар для каталога DiplomShop с описанием на русском языке.")
        );

        productRepository.saveAll(products);
        log.info("Создано товаров: {}.", products.size());
    }

    private void seedABTests() {
        if (abTestRepository.count() > 0) {
            log.info("Демонстрационные A/B-тесты уже созданы, пропуск.");
            return;
        }
        List<ABTestEntity> tests = List.of(
                buildABTest(
                        "Тест главной страницы",
                        "Вариант A: тёмная промо-страница. Вариант B: минималистичная светлая главная страница.",
                        null),
                buildABTest(
                        "Тест социальных доказательств",
                        "Проверяется влияние отзывов, рейтингов и акцентных блоков на интерес пользователя к товарам.",
                        null),
                buildABTest(
                        "Тест срочного предложения",
                        "Проверяется влияние акций, таймера и скидочных сообщений на переходы к товарам.",
                        LocalDateTime.now().plusDays(7))
        );
        abTestRepository.saveAll(tests);
        log.info("Создано демонстрационных A/B-тестов: {}.", tests.size());
    }

    private ABTestEntity buildABTest(String name, String description, LocalDateTime expiresAt) {
        ABTestEntity t = new ABTestEntity();
        t.setName(name);
        t.setDescription(description);
        t.setActive(true);
        t.setCreatedAt(LocalDateTime.now());
        t.setExpiresAt(expiresAt);
        return t;
    }

    private UserEntity buildUser(String login, String rawPassword, String firstName, String lastName,
                           String country, String language, Gender gender, int age,
                           String phone, String email, Set<String> roles) {
        UserEntity u = new UserEntity();
        u.setLogin(login);
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setCountry(country);
        u.setLanguage(language);
        u.setGender(gender);
        u.setAge(age);
        u.setPhone(phone);
        u.setEmail(email);
        u.setRoles(roles);
        u.setCreatedAt(LocalDateTime.now());
        return u;
    }

    private ProductEntity buildProduct(String name, BigDecimal price, int qty, String description) {
        ProductEntity p = new ProductEntity();
        p.setName(name);
        p.setPrice(price);
        p.setAvailableQuantity(qty);
        p.setDescription(description);
        p.setCreatedAt(LocalDateTime.now());
        return p;
    }

    private void republishAllUserProfiles() {
        List<UserEntity> allUsers = userRepository.findAll();
        allUsers.forEach(userService::publishProfile);
        log.info("Повторно опубликовано профилей пользователей в Kafka: {}.", allUsers.size());
    }
}
