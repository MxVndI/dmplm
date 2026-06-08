package com.diplom.notification.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateCampaignDto {

    @NotBlank
    private String name;
    @NotBlank
    private String channel;
    private String subject;
    @NotBlank
    private String body;
    @NotNull
    private String targetType;
    private List<String> targetUserIds;
    private String testId;
    private String testVariant;
}
