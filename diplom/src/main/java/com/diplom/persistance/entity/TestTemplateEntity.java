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
    private String testId;
    private String variant;
    private String pagePattern;
    private String pageName;
    private String minioKey;
    private String originalFileName;
    private LocalDateTime uploadedAt;
}
