package com.diplom.testservice.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Map;

@Data
public class CreateABConfigDto {

    private String id;

    @NotBlank
    private String name;

    private String description;

    @NotEmpty
    private Map<String, Integer> variants;

    private boolean active = true;
}
