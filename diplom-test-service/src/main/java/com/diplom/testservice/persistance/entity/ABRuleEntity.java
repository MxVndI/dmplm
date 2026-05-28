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

    /** Null means the rule applies to ALL users (global rule). */
    private String userId;

    private String pathPattern;

    /** References ABConfig.id that this rule activates. */
    private String abTestId;

    /** Tie-breaker: higher integer wins. */
    private int priority;

    private boolean active = true;
}
