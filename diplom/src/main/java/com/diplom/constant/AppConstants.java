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

    public static final String LOGIN_ERROR = "???????? ????? ??? ??????.";
    public static final String LOGOUT_SUCCESS = "?? ????? ?? ????????.";
    public static final String REGISTRATION_SUCCESS = "??????????? ?????? ???????. ?????? ??????? ? ???????.";
    public static final String PRODUCT_NAME_REQUIRED = "??????? ???????? ??????";
    public static final String PRICE_REQUIRED = "??????? ????";
    public static final String PRICE_POSITIVE = "???? ?????? ???? ?????? 0";
    public static final String PRICE_FORMAT_INVALID = "???? ????? ????????? ?? 10 ???? ? 2 ????? ????? ???????";
    public static final String DESCRIPTION_TOO_LONG = "???????? ??????? ???????, ???????? 2000 ????????";
    public static final String QUANTITY_REQUIRED = "??????? ?????????? ??????";
    public static final String QUANTITY_NEGATIVE = "?????????? ?? ????? ???? ?????????????";
    public static final String PRODUCT_NOT_FOUND = "Товар не найден: ";
    public static final String USER_NOT_FOUND = "Пользователь не найден";
    public static final String USER_NOT_FOUND_PREFIX = "Пользователь не найден: ";
    public static final String AB_TEST_NOT_FOUND = "A/B-???? ?? ??????: ";
    public static final String USER_ALREADY_ENROLLED = "???????????? ??? ????????? ? ???? ?????.";
    public static final String CANNOT_ENROLL_IN_INACTIVE_TEST = "?????? ???????? ?????????: ???? ?? ???????.";
    public static final String TEST_SERVICE_UNAVAILABLE = "Сервис тестирования недоступен: ";
    public static final String LOGIN_OR_EMAIL_TAKEN = "Логин или email уже занят.";
    public static final String EMAIL_TAKEN = "Email уже используется другим аккаунтом.";
    public static final String CART_IS_EMPTY = "Корзина пуста.";
    public static final String NOT_ENOUGH_STOCK = "Недостаточно товара на складе: ";
    public static final String ORDER_SUCCESS_PREFIX = "Заказ №";
    public static final String ORDER_SUCCESS_MIDDLE = " оформлен — сумма ";
    public static final String CURRENCY_RUB = " ₽";
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
