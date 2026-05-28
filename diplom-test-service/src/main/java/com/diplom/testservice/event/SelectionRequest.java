package com.diplom.testservice.event;

import com.diplom.testservice.persistance.entity.TestCriteria;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SelectionRequest {
    private String testId;
    private String testName;
    private TestCriteria criteria;
}
