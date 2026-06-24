package com.diplom.constant;

public final class AppConstants {

    public static final String VARIANT_A = "A";
    public static final String VARIANT_B = "B";
    public static final String VARIANT_C = "C";
    public static final String VARIANT_D = "D";

    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ORDER_STATUS_COMPLETED = "COMPLETED";
    public static final String SESSION_CART_KEY = "shopCart";
    public static final String TOPIC_USER_EVENTS = "user-events";
    public static final String TOPIC_USER_PROFILES = "user-profiles";
    public static final String EVENT_LOGIN = "LOGIN";
    public static final String TOPIC_SEGMENT_CHANGES = "user-segment-changes";
    public static final String TOPIC_PARTICIPANTS_RESULT = "test-participants-result";
    public static final String SHOP_GROUP_ID = "diplom-shop-group";
    public static final String AUTO_OFFSET_EARLIEST = "earliest";
    public static final String CLEANUP_POLICY_CONFIG = "cleanup.policy";
    public static final String CLEANUP_POLICY_COMPACT = "compact";
    public static final String MIN_CLEANABLE_DIRTY_RATIO_CONFIG = "min.cleanable.dirty.ratio";
    public static final String MIN_CLEANABLE_DIRTY_RATIO = "0.01";
    public static final String SEGMENT_MS_CONFIG = "segment.ms";
    public static final String SEGMENT_MS = "60000";
    public static final String REQUEST_ATTRIBUTE_AB_TEST_ID = "abTestId";
    public static final String REQUEST_ATTRIBUTE_VARIANT = "variant";
    public static final String MODEL_TRACK_USER_ID = "_trackUserId";
    public static final String MODEL_TRACK_TEST_ID = "_trackTestId";
    public static final String MODEL_TRACK_VARIANT = "_trackVariant";
    public static final String MODEL_CART_COUNT = "_cartCount";

    public static final String LOGIN_ERROR = "Неверный логин или пароль.";
    public static final String LOGOUT_SUCCESS = "Вы вышли из аккаунта.";
    public static final String REGISTRATION_SUCCESS = "Регистрация прошла успешно. Теперь войдите в аккаунт.";
    public static final String PRODUCT_NAME_REQUIRED = "Укажите название товара";
    public static final String PRICE_REQUIRED = "Укажите цену";
    public static final String PRICE_POSITIVE = "Цена должна быть больше 0";
    public static final String PRICE_FORMAT_INVALID = "Цена может содержать до 10 цифр и 2 знака после запятой";
    public static final String DESCRIPTION_TOO_LONG = "Описание слишком длинное, максимум 2000 символов";
    public static final String QUANTITY_REQUIRED = "Укажите количество товара";
    public static final String QUANTITY_NEGATIVE = "Количество не может быть отрицательным";
    public static final String PRODUCT_NOT_FOUND = "Товар не найден: ";
    public static final String USER_NOT_FOUND = "Пользователь не найден";
    public static final String USER_NOT_FOUND_PREFIX = "Пользователь не найден: ";
    public static final String AB_TEST_NOT_FOUND = "A/B-тест не найден: ";
    public static final String USER_ALREADY_ENROLLED = "Пользователь уже участвует в этом тесте.";
    public static final String CANNOT_ENROLL_IN_INACTIVE_TEST = "Нельзя добавить участника: тест не активен.";
    public static final String TEST_SERVICE_UNAVAILABLE = "Сервис тестирования недоступен: ";
    public static final String LOGIN_OR_EMAIL_TAKEN = "Логин или email уже занят.";
    public static final String EMAIL_TAKEN = "Email уже используется другим аккаунтом.";
    public static final String CART_IS_EMPTY = "Корзина пуста.";
    public static final String NOT_ENOUGH_STOCK = "Недостаточно товара на складе: ";
    public static final String ORDER_SUCCESS_PREFIX = "Заказ №";
    public static final String ORDER_SUCCESS_MIDDLE = " оформлен — сумма ";
    public static final String CURRENCY_RUB = " ₽";
    public static final String LOGIN_REQUIRED = "Укажите логин";
    public static final String LOGIN_SIZE_INVALID = "Логин должен содержать от 3 до 50 символов";
    public static final String LOGIN_FORMAT_INVALID = "Логин может содержать только буквы, цифры и подчёркивание";
    public static final String PASSWORD_REQUIRED = "Укажите пароль";
    public static final String PASSWORD_SIZE_INVALID = "Пароль должен содержать не менее 8 символов";
    public static final String FIRST_NAME_REQUIRED = "Укажите имя";
    public static final String LAST_NAME_REQUIRED = "Укажите фамилию";
    public static final String COUNTRY_REQUIRED = "Укажите страну";
    public static final String LANGUAGE_REQUIRED = "Укажите язык";
    public static final String GENDER_REQUIRED = "Укажите пол";
    public static final String AGE_REQUIRED = "Укажите возраст";
    public static final String AGE_POSITIVE = "Возраст должен быть положительным";
    public static final String AGE_INVALID = "Проверьте корректность возраста";
    public static final String PHONE_INVALID = "Неверный формат телефона";
    public static final String EMAIL_REQUIRED = "Укажите email";
    public static final String EMAIL_INVALID = "Неверный формат email";
    public static final String TELEGRAM_CHAT_ID_INVALID = "Telegram chat ID должен быть числовым";
    public static final String INCOME_LEVEL_INVALID = "Уровень дохода должен быть LOW, MEDIUM или HIGH";
    public static final String EDUCATION_LEVEL_INVALID = "Уровень образования должен быть BASIC, SECONDARY, HIGHER или ACADEMIC";
    public static final String OCCUPATION_INVALID = "Укажите корректный тип занятости";

    public static final String FALLBACK_FILE_NAME = "file";
    public static final String PRODUCTS_FOLDER = "products/";

    public static final String REDIRECT_PREFIX = "redirect:";
    public static final String FORWARD_PREFIX = "forward:";
    public static final String REDIRECT_CART = "redirect:/cart";
    public static final String REDIRECT_LOGIN = "redirect:/auth/login";
    public static final String DEFAULT_VIEW_NAME = "index";
    public static final String TEMPLATE_TEST_PAGE = "user/test-template-page";
    public static final String TEMPLATES_CLASSPATH_PREFIX = "classpath:/templates/";
    public static final String HTML_EXTENSION = ".html";

    private AppConstants() {
    }
}
