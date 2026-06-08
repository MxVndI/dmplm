package com.diplom.persistance.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@Document(collection = "user_events")
public class UserEventEntity {

    @Id
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
