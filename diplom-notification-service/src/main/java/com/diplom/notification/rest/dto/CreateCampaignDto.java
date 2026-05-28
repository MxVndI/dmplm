package com.diplom.notification.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateCampaignDto {

    @NotBlank
    private String name;

    /** EMAIL, TELEGRAM, or BOTH */
    @NotBlank
    private String channel;

    /** Email subject (required when channel is EMAIL or BOTH) */
    private String subject;

    @NotBlank
    private String body;

    /** ALL or SPECIFIC */
    @NotNull
    private String targetType;

    /** Required when targetType=SPECIFIC */
    private List<String> targetUserIds;

    /** Optional: link to an A/B test */
    private String testId;

    /** A | B | null — send only to this variant's participants */
    private String testVariant;
}
