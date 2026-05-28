package com.diplom.notification.rest.dto;

import lombok.Data;

@Data
public class CreateAbCampaignDto {

    private String baseName;

    private String channel;

    private String testId;

    private String subjectA;
    private String bodyA;

    private String subjectB;
    private String bodyB;
}
