package com.diplom.testservice.constant;

public final class AppConstants {

    public static final String TEST_NAME_REQUIRED = "Название теста обязательно";
    public static final String CRITERIA_REQUIRED = "Критерии отбора обязательны";
    public static final String VARIANT_A = "A";
    public static final String VARIANT_B = "B";
    public static final String TOPIC_SELECTION_REQUESTS = "test-selection-requests";
    public static final String TOPIC_PARTICIPANTS_RESULT = "test-participants-result";
    public static final String TEST_SERVICE_GROUP_ID = "diplom-test-service-group";
    public static final String AUTO_OFFSET_EARLIEST = "earliest";
    public static final String TEST_NOT_FOUND = "Тест не найден: ";
    public static final String COMPLETED_TEST_SELECTION_FORBIDDEN = "Нельзя запускать отбор для завершённого теста.";
    public static final String COMPLETED_TEST_EDIT_FORBIDDEN = "Нельзя редактировать завершённый тест.";

    private AppConstants() {
    }
}
