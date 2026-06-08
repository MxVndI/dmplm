package com.diplom.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestParticipantEvent {

    private String testId;
    private String userId;
    private String variant;
    private Instant enrolledAt;
}
