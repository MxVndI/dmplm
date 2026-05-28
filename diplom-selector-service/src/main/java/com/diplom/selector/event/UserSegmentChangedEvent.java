package com.diplom.selector.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSegmentChangedEvent {

    private String userId;

    /** Segment before the transition, or null for a first-time assignment. */
    private String previousSegment;

    /** The new segment the user was assigned to. */
    private String newSegment;

    /** Epoch milliseconds of the event that triggered the transition. */
    private long changedAt;
}
