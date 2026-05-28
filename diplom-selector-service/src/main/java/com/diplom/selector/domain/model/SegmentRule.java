package com.diplom.selector.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SegmentRule {

    private String name;
    private int priority;
    private String logic = "AND";
    private List<SegmentCondition> conditions;
}
