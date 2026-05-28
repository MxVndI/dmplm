package com.diplom.selector.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserAggregateState {

    private String userId;

    private List<Long> visitTimestamps = new ArrayList<>();

    private int visitCount7Days;

    private int purchaseCount;
    private double totalSpent;

    private int cartAddCount;

    private Long lastCartAddTimestamp;

    private boolean purchasedAfterLastCart = true;

    private int productViewCount;

    private Long firstEventTimestamp;

    private Long lastEventTimestamp;

    private String currentSegment;

    private Long segmentChangedAt;
}
