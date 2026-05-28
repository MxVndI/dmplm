package com.diplom.selector.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DemographicProfile {
    private String userId;
    private String incomeLevel;
    private String educationLevel;
    private String occupation;
    private List<String> interests;
}
