package com.diplom.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Placeholder model for user behaviour metrics.
 *
 * Fields are intentionally broad so that any type of front-end event
 * (page view, click, scroll depth, search query, add-to-cart, etc.)
 * can be stored without schema changes.
 *
 * Full implementation will be added in a future iteration.
 */
@Data
@NoArgsConstructor
public class UserEvent {

    private String id;

    private String userId;
    private String sessionId;

    /** e.g. PAGE_VIEW, CLICK, SEARCH, ADD_TO_CART, CHECKOUT */
    private String eventType;

    /** Flexible key-value payload specific to the event type. */
    private Map<String, Object> eventData;

    /** Route / URL where the event occurred. */
    private String page;

    /** A/B test context — populated when user is in a test. */
    private String testId;
    private String variant;

    private String userAgent;
    private String ipAddress;
    private LocalDateTime timestamp;
}
