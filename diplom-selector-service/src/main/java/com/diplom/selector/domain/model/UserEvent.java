package com.diplom.selector.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserEvent {

    private String userId;

    private String eventType;

    private long timestamp;

    private String page;

    private Double amount;

    private String sessionId;
}
