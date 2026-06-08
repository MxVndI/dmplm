package com.diplom.testservice.persistance.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@Document(collection = "ab_assignments")
@CompoundIndex(def = "{'userId': 1, 'abTestId': 1}", unique = true)
public class ABAssignmentEntity {

    @Id
    private String id;
    private String userId;
    private String abTestId;
    private String variant;
    private Instant assignedAt;
}
