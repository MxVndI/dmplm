package com.diplom.notification.persistance.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/** Individual delivery record for a single user in a campaign. */
@Data
@NoArgsConstructor
@Document(collection = "notification_deliveries")
public class NotificationDeliveryEntity {

    @Id
    private String id;

    private String campaignId;
    private String userId;

    /** Channel used: EMAIL or TELEGRAM */
    private String channel;

    private String recipient; // email address or Telegram chat ID

    private DeliveryStatus status;
    private String errorMessage;

    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
}
