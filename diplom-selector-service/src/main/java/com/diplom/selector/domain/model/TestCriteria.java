package com.diplom.selector.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestCriteria {
    private Integer ageMin;
    private Integer ageMax;
    private List<String> countries;
    private List<String> languages;
    private List<String> genders;
    private List<String> incomeLevels;
    private List<String> educationLevels;
    private List<String> occupations;
    private List<String> interests;
    private Integer maxParticipants;
    private Integer variantARatio;
}
