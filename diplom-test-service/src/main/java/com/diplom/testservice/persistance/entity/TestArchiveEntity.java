package com.diplom.testservice.persistance.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "test_archives")
public class TestArchiveEntity {

    @Id
    private String id;

    private String testId;
    private String name;
    private String description;

    private LocalDateTime completedAt;
    private LocalDateTime archivedAt;

    private long durationMinutes;

    private long variantACount;
    private long variantAConversions;
    private double variantAConversionRate;
    private double variantAAvgValuePerUser;
    private double variantATotalValue;

    private long variantBCount;
    private long variantBConversions;
    private double variantBConversionRate;
    private double variantBAvgValuePerUser;
    private double variantBTotalValue;

    private long totalParticipants;
    private long totalConversions;
    private double overallConversionRate;
}
