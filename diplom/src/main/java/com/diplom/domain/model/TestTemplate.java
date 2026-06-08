package com.diplom.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class TestTemplate {

    private String id;
    private String name;
    private String testId;
    private String variant;
    private String pagePattern;
    private String pageName;
    private String minioKey;
    private String originalFileName;
    private LocalDateTime uploadedAt;
}
