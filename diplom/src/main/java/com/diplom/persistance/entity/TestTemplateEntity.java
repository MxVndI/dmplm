package com.diplom.persistance.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document(collection = "test_templates")
@CompoundIndex(def = "{'testId': 1, 'variant': 1, 'pagePattern': 1}", unique = true)
public class TestTemplateEntity {

    @Id
    private String id;

    private String name;

    /** Which A/B test this template belongs to */
    private String testId;

    /** "A", "B", "C", "D" */
    private String variant;

    /**
     * URL pattern this template covers.
     * Supports exact paths (/products), single-segment wildcards (/products/*),
     * and multi-segment wildcards (/products/**).
     */
    private String pagePattern;

    /** Human-readable page label, e.g. "Главная страница" */
    private String pageName;

    /** MinIO object key — e.g. "templates/66abc123.html" */
    private String minioKey;

    private String originalFileName;

    private LocalDateTime uploadedAt;
}
