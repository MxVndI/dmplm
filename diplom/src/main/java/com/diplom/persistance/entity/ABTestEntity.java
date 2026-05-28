package com.diplom.persistance.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document(collection = "ab_tests")
public class ABTestEntity {

    @Id
    private String id;

    private String name;
    private String description;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime endedAt;

    /** Optional deadline — test is auto-deactivated after this point. */
    private LocalDateTime expiresAt;
}
