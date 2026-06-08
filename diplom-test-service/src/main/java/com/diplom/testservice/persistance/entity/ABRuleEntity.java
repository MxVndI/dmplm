package com.diplom.testservice.persistance.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Document(collection = "ab_rules")
public class ABRuleEntity {

    @Id
    private String id;
    private String userId;
    private String pathPattern;
    private String abTestId;
    private int priority;
    private boolean active = true;
}
