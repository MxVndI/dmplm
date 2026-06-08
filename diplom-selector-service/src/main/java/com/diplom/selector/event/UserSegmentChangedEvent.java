package com.diplom.selector.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSegmentChangedEvent {

    private String userId;
    private String previousSegment;
    private String newSegment;
    private long changedAt;
}
