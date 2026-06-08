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
        UserEntity admin = buildUser("admin", "Admin1234!", "Admin", "User",
                "Russia", "Russian", Gender.OTHER, 30, "+7 999 000-00-00",
                "admin@diplom.local", Set.of("ROLE_USER", "ROLE_ADMIN"));
        UserEntity saved = userRepository.save(admin);
        userService.publishProfile(saved);
        log.warn("=== Default admin created: login=admin  password=Admin1234! — CHANGE THIS! ===");
    }

    private void seedUsers() {
        if (userRepository.existsByLogin("ivan_petrov")) {
            log.info("Users already seeded, skipping.");
            return;
        }

        List<UserEntity> users = List.of(
                buildUser("ivan_petrov",   "Pass1234!", "Ivan",      "Petrov",    "Russia",       "Russian",  Gender.MALE,   25, "+7 911 111-11-11", "ivan.petrov@mail.ru",      Set.of("ROLE_USER")),
                buildUser("maria_ivanova", "Pass1234!", "Maria",     "Ivanova",   "Russia",       "Russian",  Gender.FEMALE, 32, "+7 922 222-22-22", "maria.ivanova@mail.ru",    Set.of("ROLE_USER")),
                buildUser("alex_sidorov",  "Pass1234!", "Alex",      "Sidorov",   "Russia",       "Russian",  Gender.MALE,   19, "+7 933 333-33-33", "alex.sidorov@yandex.ru",   Set.of("ROLE_USER")),
                buildUser("olga_koval",    "Pass1234!", "Olga",      "Koval",     "Ukraine",      "Ukrainian",Gender.FEMALE, 27, "+380 99 111-22-33","olga.koval@gmail.com",      Set.of("ROLE_USER")),
                buildUser("dmitry_novo",   "Pass1234!", "Dmitry",    "Novoselov", "Russia",       "Russian",  Gender.MALE,   41, "+7 944 444-44-44", "dmitry.novo@mail.ru",      Set.of("ROLE_USER")),
                buildUser("anna_white",    "Pass1234!", "Anna",      "White",     "Germany",      "German",   Gender.FEMALE, 23, "+49 176 1234567",  "anna.white@gmail.com",     Set.of("ROLE_USER")),
                buildUser("john_smith",    "Pass1234!", "John",      "Smith",     "USA",          "English",  Gender.MALE,   35, "+1 555 987-6543",  "john.smith@gmail.com",     Set.of("ROLE_USER")),
                buildUser("sophie_martin", "Pass1234!", "Sophie",    "Martin",    "France",       "French",   Gender.FEMALE, 29, "+33 6 12 34 56 78","sophie.martin@gmail.com",  Set.of("ROLE_USER")),
                buildUser("ali_hassan",    "Pass1234!", "Ali",       "Hassan",    "Kazakhstan",   "Russian",  Gender.MALE,   22, "+7 701 555-66-77", "ali.hassan@mail.ru",       Set.of("ROLE_USER")),
                buildUser("zara_bekova",   "Pass1234!", "Zara",      "Bekova",    "Kazakhstan",   "Russian",  Gender.FEMALE, 18, "+7 702 888-99-00", "zara.bekova@mail.ru",      Set.of("ROLE_USER")),
                buildUser("sergey_mak",    "Pass1234!", "Sergey",    "Makarov",   "Belarus",      "Russian",  Gender.MALE,   37, "+375 29 111-22-33","sergey.mak@tut.by",        Set.of("ROLE_USER")),
                buildUser("elena_volkov",  "Pass1234!", "Elena",     "Volkova",   "Russia",       "Russian",  Gender.FEMALE, 45, "+7 955 100-20-30", "elena.volkov@mail.ru",     Set.of("ROLE_USER")),
                buildUser("nikita_frost",  "Pass1234!", "Nikita",    "Frost",     "Russia",       "Russian",  Gender.MALE,   20, "+7 966 200-30-40", "nikita.frost@yandex.ru",   Set.of("ROLE_USER")),
                buildUser("yuki_tanaka",   "Pass1234!", "Yuki",      "Tanaka",    "Japan",        "Japanese", Gender.FEMALE, 26, "+81 90-1234-5678", "yuki.tanaka@gmail.com",    Set.of("ROLE_USER")),
                buildUser("carlos_garcia", "Pass1234!", "Carlos",    "Garcia",    "Spain",        "Spanish",  Gender.MALE,   33, "+34 612 345 678",  "carlos.garcia@gmail.com",  Set.of("ROLE_USER"))
        );

        for (UserEntity u : users) {
            if (!userRepository.existsByLogin(u.getLogin())) {
                UserEntity saved = userRepository.save(u);
                userService.publishProfile(saved);
            }
        }
        log.info("Seeded {} test users.", users.size());
    }

    private static final int EXPECTED_PRODUCT_COUNT = 32;

    private void seedProducts() {
        long count = productRepository.count();
        if (count >= EXPECTED_PRODUCT_COUNT) {
            log.info("Products already fully seeded ({} items), skipping.", count);
            return;
        }
        if (count > 0) {
            productRepository.deleteAll();
            log.info("Cleared {} old products to reseed with full catalog.", count);
        }

        List<ProductEntity> products = List.of(
                buildProduct("Wireless Noise-Cancelling Headphones",
                        new BigDecimal("149.99"), 50,
                        "Premium over-ear headphones with 30h battery and ANC technology."),
                buildProduct("Mechanical Keyboard RGB",
                        new BigDecimal("89.90"), 120,
                        "Compact TKL mechanical keyboard with hot-swap switches and per-key RGB."),
                buildProduct("Wireless Earbuds True ANC",
                        new BigDecimal("79.99"), 150,
                        "Active noise cancellation, 28h total battery, IPX4 water resistance, fast charge."),
                buildProduct("Gaming Headset 7.1 Surround",
                        new BigDecimal("109.00"), 45,
                        "Virtual 7.1 surround sound, noise-cancelling boom mic, USB + 3.5 mm jack."),
                buildProduct("USB Condenser Microphone",
                        new BigDecimal("129.00"), 25,
                        "Cardioid pattern, 96 kHz/24-bit, zero-latency monitoring, plug-and-play USB-C."),
                buildProduct("Wireless Gaming Mouse 25K DPI",
                        new BigDecimal("69.00"), 80,
                        "25 600 DPI optical sensor, 70h battery, 2.4 GHz + BT dual-mode, 6 programmable buttons."),

                buildProduct("4K USB-C Monitor 27\"",
                        new BigDecimal("399.00"), 15,
                        "IPS panel, 3840×2160, 60 Hz, USB-C 65 W power delivery, sRGB 99%."),
                buildProduct("Webcam 4K 60 fps Autofocus",
                        new BigDecimal("159.00"), 30,
                        "Sony STARVIS sensor, auto light correction, built-in stereo mic, USB-C."),
                buildProduct("Adjustable Laptop Stand",
                        new BigDecimal("55.00"), 90,
                        "Aluminium alloy, 6 height levels, foldable, compatible with laptops 10–17 \"."),

                buildProduct("Portable SSD 1 TB",
                        new BigDecimal("79.95"), 200,
                        "USB 3.2 Gen2, up to 1000 MB/s read speed, shock-resistant metal casing."),
                buildProduct("USB-C 10-Port Hub",
                        new BigDecimal("45.90"), 200,
                        "4× USB-A 3.0, 2× USB-C, HDMI 4K, SD/microSD, 100 W PD pass-through."),
                buildProduct("65 W GaN Charger 4-Port",
                        new BigDecimal("49.99"), 170,
                        "Gallium Nitride tech, 2× USB-C PD + 2× USB-A, charges laptop + phone simultaneously."),
                buildProduct("Qi2 Wireless Charger Stand",
                        new BigDecimal("39.99"), 120,
                        "15 W Qi2 MagSafe compatible, watch + earbuds charging spots, braided cable."),

                buildProduct("Smart Watch 45mm",
                        new BigDecimal("249.00"), 60,
                        "AMOLED display, heart rate, SpO2, GPS, 7-day battery, iOS & Android."),
                buildProduct("Smart Body Scale Wi-Fi",
                        new BigDecimal("69.00"), 55,
                        "13 body metrics, BMI, muscle/fat mass, cloud sync, app for iOS & Android."),
                buildProduct("Smart LED Bulb 4-pack",
                        new BigDecimal("34.99"), 100,
                        "RGBW 16M colours, 800 lm, E27, Wi-Fi, voice control (Alexa, Google, Siri)."),

                buildProduct("Mini Projector 1080p",
                        new BigDecimal("289.00"), 12,
                        "Full HD 1080p, 800 ANSI lm, built-in Android 11, Wi-Fi & BT, 30 000 h lamp life."),

                buildProduct("Ergonomic Office Chair",
                        new BigDecimal("329.00"), 8,
                        "Adjustable lumbar support, mesh back, 3D armrests, max load 120 kg."),
                buildProduct("Standing Desk Converter",
                        new BigDecimal("189.00"), 22,
                        "Sit-stand workstation, 35 kg capacity, height range 15–40 cm, anti-fatigue mat included."),
                buildProduct("LED Desk Lamp with USB-C",
                        new BigDecimal("49.99"), 65,
                        "5 colour temperatures, 10 brightness levels, wireless charging base, foldable arm."),

                buildProduct("Coffee Capsule Machine",
                        new BigDecimal("119.00"), 35,
                        "Compatible with Nespresso pods, 19-bar pump, 0.7 L tank, 25-sec heat-up."),
                buildProduct("High-Speed Blender 1200 W",
                        new BigDecimal("89.00"), 35,
                        "1200 W motor, 6 stainless blades, 1.5 L BPA-free jug, pulse and smoothie modes."),
                buildProduct("HEPA Air Purifier 40 m²",
                        new BigDecimal("199.00"), 20,
                        "True HEPA H13 + activated carbon filter, 40 m² coverage, night mode, air quality indicator."),
                buildProduct("Aromatherapy Diffuser 500 ml",
                        new BigDecimal("29.99"), 80,
                        "Ultrasonic mist, 7-colour LED, timer, auto-off, BPA-free — perfect for offices."),
                buildProduct("Bamboo Cutting Board 3-pack",
                        new BigDecimal("34.99"), 110,
                        "S/M/L sizes, food-safe bamboo, juice groove, non-slip feet, dishwasher safe."),

                buildProduct("Robot Vacuum Cleaner 4500 Pa",
                        new BigDecimal("349.00"), 10,
                        "4500 Pa suction, LiDAR mapping, mop function, self-emptying dock, app + voice control."),

                buildProduct("Resistance Bands Set 5-piece",
                        new BigDecimal("24.99"), 200,
                        "5 resistance levels 5–40 kg, natural latex, includes carry bag and guide booklet."),
                buildProduct("Non-Slip Yoga Mat 6 mm",
                        new BigDecimal("39.99"), 150,
                        "TPE eco-friendly, 183×61 cm, 6 mm cushioning, alignment lines, carry strap included."),
                buildProduct("Percussion Massage Gun 30-speed",
                        new BigDecimal("149.00"), 30,
                        "30 speed levels, 2400 rpm, 6 head attachments, 8h battery, whisper-quiet 45 dB."),
                buildProduct("Jump Rope with Digital Counter",
                        new BigDecimal("19.99"), 200,
                        "LCD counter tracks jumps & calories, ball-bearing handles, adjustable 3 m cable."),

                buildProduct("RFID Leather Wallet Slim",
                        new BigDecimal("44.99"), 120,
                        "Genuine top-grain leather, RFID-blocking, 8 card slots, 2 cash pockets, gift box."),
                buildProduct("Waterproof Backpack 30 L",
                        new BigDecimal("79.99"), 60,
                        "30 L, IPX5 water resistance, USB charging port, padded laptop sleeve (up to 17\"), TSA lock.")
        );

        productRepository.saveAll(products);
        log.info("Seeded {} products.", products.size());
    }

    private void seedABTests() {
        if (abTestRepository.count() > 0) {
            log.info("AB tests already seeded, skipping.");
            return;
        }
        List<ABTestEntity> tests = List.of(
                buildABTest(
                        "Тест яркого лейаута vs минимализма",
                        "Вариант A: яркий хайп-стиль (неон, крупные баннеры). " +
                        "Вариант B: чистый бутик-стиль (белый фон, элегантная типографика). " +
                        "Цель: определить, какой стиль увеличивает конверсию.",
                        null),
                buildABTest(
                        "Тест социальных доказательств",
                        "Вариант A: стандартная главная. " +
                        "Вариант C: усиленный акцент на отзывы, звёзды и рейтинги товаров. " +
                        "Цель: проверить влияние социальных доказательств на добавление в корзину.",
                        null),
                buildABTest(
                        "Тест Flash-распродажи",
                        "Вариант A: стандартный каталог товаров. " +
                        "Вариант D: страница с таймером обратного отсчёта и ценниками «скидка -X%». " +
                        "Цель: оценить эффект срочности на количество покупок.",
                        LocalDateTime.now().plusDays(7))
        );
        abTestRepository.saveAll(tests);
        log.info("Seeded {} demo A/B tests.", tests.size());
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
        log.info("Re-published {} user profile(s) to Kafka 'user-profiles' topic.", allUsers.size());
    }
}
