package com.diplom.testservice.rest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateABRuleDto {

    private String userId;

    private String pathPattern;

    @NotBlank
    private String abTestId;

    private int priority = 1;

    private boolean active = true;
}
