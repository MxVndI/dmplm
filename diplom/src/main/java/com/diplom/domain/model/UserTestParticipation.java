package com.diplom.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Records a user's participation in an A/B test.
 * Contains: test id, user id, assigned variant (A or B).
 */
@Data
@NoArgsConstructor
@Document(collection = "user_test_participations")
@CompoundIndex(def = "{'testId': 1, 'userId': 1}", unique = true)
public class UserTestParticipation {

    private String id;

    private String testId;
    private String userId;

    /** Variant assigned to the user: "A" or "B". */
    private String variant;

    private LocalDateTime enrolledAt;
}
