package com.diplom.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document(collection = "user_test_participations")
@CompoundIndex(def = "{'testId': 1, 'userId': 1}", unique = true)
public class UserTestParticipation {

    private String id;
    private String testId;
    private String userId;
    private String variant;
    private LocalDateTime enrolledAt;
}
