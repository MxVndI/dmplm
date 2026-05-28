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

    /** Human-readable campaign name */
    private String name;

    /** Channel: EMAIL, TELEGRAM, or BOTH */
    private String channel;

    /** Subject line (email only) */
    private String subject;

    /** Message body (plain text for Telegram, HTML for email) */
    private String body;

    /** Target: ALL, or specific user IDs */
    private String targetType; // ALL | SPECIFIC

    /** Specific recipient user IDs (when targetType=SPECIFIC) */
    private List<String> targetUserIds;

    /** Optional: link this campaign to an A/B test */
    private String testId;

    /** Optional: send only to variant A or B participants */
    private String testVariant; // A | B | null (both)

    private CampaignStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime sentAt;

    /** How many messages were sent successfully */
    private int sentCount;

    /** How many failed */
    private int failedCount;
}
