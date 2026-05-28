package com.diplom.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Stores metadata about an uploaded Thymeleaf/HTML template
 * that can be shown to A/B test participants as the shop home page.
 *
 * The actual HTML content is stored in MinIO under key {@code templates/<id>.html}.
 */
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
