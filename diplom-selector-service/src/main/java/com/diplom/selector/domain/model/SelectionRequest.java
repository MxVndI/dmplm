package com.diplom.selector.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SelectionRequest {
    private String testId;
    private String testName;
    private TestCriteria criteria;
}
