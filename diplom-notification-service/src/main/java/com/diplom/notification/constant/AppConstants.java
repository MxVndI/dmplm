package com.diplom.notification.constant;

public final class AppConstants {

    public static final String VARIANT_A = "A";
    public static final String VARIANT_B = "B";
    public static final String VARIANT_A_SUFFIX = " [Вариант A]";
    public static final String VARIANT_B_SUFFIX = " [Вариант B]";
    public static final String TARGET_SPECIFIC = "SPECIFIC";
    public static final String TARGET_ALL = "ALL";
    public static final String CHANNEL_BOTH = "BOTH";
    public static final String CHANNEL_EMAIL = "EMAIL";
    public static final String CHANNEL_TELEGRAM = "TELEGRAM";

    public static final String DEFAULT_CHANNEL = "default";
    public static final String ADMIN_CHANNEL = "admin";
    public static final String ERROR_FIELD = "error";
    public static final String STATUS_FIELD = "status";
    public static final String SERVICE_FIELD = "service";
    public static final String TEXT_REQUIRED = "????? ??????????? ??????????";
    public static final String MESSAGE_REQUIRED = "????????? ???????????";
    public static final String STATUS_SENT = "??????????";
    public static final String STATUS_ALERT_SENT = "???????????_??????????";
    public static final String STATUS_HEALTHY = "????????";
    public static final String TELEGRAM_NOTIFICATION_SERVICE = "telegram-notification";
    public static final String CAMPAIGN_NOT_FOUND = "Кампания не найдена: ";
    public static final String CAMPAIGN_ALREADY_SENT = "Кампания уже отправлена";
    public static final String CANNOT_DELETE_SENT_CAMPAIGN = "Нельзя удалить уже отправленную кампанию";

    private AppConstants() {
    }
}
