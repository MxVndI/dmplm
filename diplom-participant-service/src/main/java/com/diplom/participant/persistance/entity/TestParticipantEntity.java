package com.diplom.participant.persistance.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "test_participants")
@CompoundIndex(name = "test_user_unique", def = "{'testId':1,'userId':1}", unique = true)
public class TestParticipantEntity {
    @Id
    private String id;
    private String testId;
    private String userId;
    private String variant;
    private Instant enrolledAt;
}
