package com.diplom.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
public class UserEvent {

    private String id;
    private String userId;
    private String sessionId;
    private String eventType;
    private Map<String, Object> eventData;
    private String page;
    private String testId;
    private String variant;
    private String userAgent;
    private String ipAddress;
    private LocalDateTime timestamp;
}
