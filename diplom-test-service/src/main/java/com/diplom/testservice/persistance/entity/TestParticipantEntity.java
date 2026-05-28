package com.diplom.testservice.persistance.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@Document(collection = "test_participants")
@CompoundIndex(def = "{'testId': 1, 'userId': 1}", unique = true)
public class TestParticipantEntity {

    @Id
    private String id;

    @Indexed
    private String testId;

    @Indexed
    private String userId;

    private String variant;
    private Instant enrolledAt;
}
