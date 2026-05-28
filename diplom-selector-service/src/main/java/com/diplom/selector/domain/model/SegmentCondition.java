package com.diplom.selector.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SegmentCondition {

    private String field;
    private String operator;
    private double value;
}
