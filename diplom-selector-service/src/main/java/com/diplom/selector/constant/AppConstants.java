package com.diplom.selector.constant;

public final class AppConstants {

    public static final String VARIANT_A = "A";
    public static final String VARIANT_B = "B";
    public static final int FALLBACK_CLUSTER_ID = -1;

    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";
    public static final String CLUSTER_ASSIGN_PATH = "/api/cluster/assign";

    public static final String AGGREGATE_STORE = "user-aggregate-store";
    public static final String USER_PROFILES_STORE = "user-profiles-store";
    public static final String TOPIC_USER_EVENTS = "user-events";
    public static final String TOPIC_SEGMENT_CHANGES = "user-segment-changes";
    public static final String TOPIC_USER_PROFILES = "user-profiles";
    public static final String TOPIC_SELECTION_REQUESTS = "test-selection-requests";
    public static final String TOPIC_PARTICIPANTS_RESULT = "test-participants-result";
    public static final String CLEANUP_POLICY_CONFIG = "cleanup.policy";
    public static final String CLEANUP_POLICY_COMPACT = "compact";

    public static final long MILLIS_PER_DAY = 86_400_000L;
    public static final long MILLIS_PER_HOUR = 3_600_000L;
    public static final double MAX_DAYS_SINCE_EVENT = 365.0;
    public static final double MAX_HOURS_SINCE_CART = 730.0;

    private AppConstants() {
    }
}
