package com.diplom.testservice.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ABResolutionDto {
    private String abTestId;
    private String variant;
}
