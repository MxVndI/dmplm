package com.diplom.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Published to Kafka topic {@code user-profiles} after every user create/update.
 * Contains ONLY the fields needed for A/B test selection (no PII beyond what the
 * test-criteria requires).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileEvent {
    private String id;
    private Integer age;
    private String country;
    private String language;
    private String gender; // plain String for cross-service compatibility
}
