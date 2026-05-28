package com.diplom.selector.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/** Forwarded to topic {@code test-participants-result} for downstream consumers. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestParticipantEvent {
    private String testId;
    private String userId;
    private String variant;
    private Instant enrolledAt;
}
