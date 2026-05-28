package com.diplom.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Consumed from Kafka topic {@code test-participants-result}.
 * Published by diplom-selector-service after a selection run.
 * The shop syncs these into its local user_test_participations collection.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestParticipantEvent {
    private String testId;
    private String userId;
    private String variant;   // "A" or "B"
    private Instant enrolledAt;
}
