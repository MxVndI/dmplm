package com.diplom.notification.persistance.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document(collection = "notification_deliveries")
public class NotificationDeliveryEntity {

    @Id
    private String id;
    private String campaignId;
    private String userId;
    private String channel;
    private String recipient;
    private DeliveryStatus status;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
}
