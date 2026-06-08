package com.diplom.notification.persistance.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@Document(collection = "notification_campaigns")
public class NotificationCampaignEntity {

    @Id
    private String id;
    private String name;
    private String channel;
    private String subject;
    private String body;
    private String targetType;
    private List<String> targetUserIds;
    private String testId;
    private String testVariant;
    private CampaignStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private int sentCount;
    private int failedCount;
}
