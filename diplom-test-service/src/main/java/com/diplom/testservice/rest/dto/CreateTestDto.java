package com.diplom.testservice.rest.dto;

import com.diplom.testservice.constant.AppConstants;
import com.diplom.testservice.persistance.entity.TestCriteria;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTestDto {

    @NotBlank(message = AppConstants.TEST_NAME_REQUIRED)
    private String name;

    private String description;

    @NotNull(message = AppConstants.CRITERIA_REQUIRED)
    @Valid
    private TestCriteria criteria;

    private java.time.LocalDateTime expiresAt;
}
