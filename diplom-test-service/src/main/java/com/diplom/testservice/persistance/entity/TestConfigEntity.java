package com.diplom.testservice.persistance.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document(collection = "test_configs")
public class TestConfigEntity {

    @Id
    private String id;
    private String name;
    private String description;
    private TestCriteria criteria;
    private TestStatus status;
    private int enrolledCount;
    private LocalDateTime createdAt;
    private LocalDateTime triggeredAt;
    private LocalDateTime completedAt;
    private LocalDateTime expiresAt;
}
