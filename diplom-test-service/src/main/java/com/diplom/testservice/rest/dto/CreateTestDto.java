package com.diplom.testservice.rest.dto;

import com.diplom.testservice.persistance.entity.TestCriteria;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTestDto {

    @NotBlank(message = "Test name is required")
    private String name;

    private String description;

    @NotNull(message = "Criteria are required")
    @Valid
    private TestCriteria criteria;

    private java.time.LocalDateTime expiresAt;
}
