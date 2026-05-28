package com.diplom.testservice.persistance.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class TestCriteria {

    /** Minimum user age (inclusive). Null = no lower bound. */
    private Integer ageMin;

    /** Maximum user age (inclusive). Null = no upper bound. */
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
